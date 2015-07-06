package io.github.tdhd.robakka

import language.postfixOps
import scala.concurrent.duration._
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorLogging
import akka.actor.Props
import akka.actor.Terminated
import akka.actor.Cancellable
import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import scala.concurrent.Future
import scala.reflect.{ ClassTag, classTag }

object World {
  // world -> world
  case object AnnounceWorldState
  // agent -> world
  case object GetUniqueID
  // world -> agent
  case class UniqueID(id: Long)
  // entity -> world
  case class RemoveEntity(entity: GameEntity)
  // entity -> world
  case class UpdateEntity(entity: GameEntity)

  // agent -> plant
  case class ConsumePlant(plant: PlantEntity, by: AgentEntity)
  // plant -> agent
  case class PlantConsumed(energy: Double)

  // elements of the game
  sealed trait GameEntity {
    def position: Location
    def id: Long
  }
  case class AgentEntity(position: Location,
    id: Long,
    team: Long,
    health: Double,
    selfRef: ActorRef,
    world: ActorRef) extends GameEntity
  case class PlantEntity(position: Location,
    id: Long,
    energy: Double,
    selfRef: ActorRef) extends GameEntity

  case class Location(row: Int, col: Int) {
    def translate(move: Agent.MoveCommand, ws: World.Size) = {
      val updated = move match {
        case Agent.MoveUpLeft => this.copy(row = row + 1, col = col - 1)
        case Agent.MoveUp => this.copy(row = row + 1)
        case Agent.MoveUpRight => this.copy(row = row + 1, col = col + 1)

        case Agent.MoveLeft => this.copy(col = col - 1)
        case Agent.MoveRight => this.copy(col = col + 1)

        case Agent.MoveDownLeft => this.copy(row = row - 1, col = col - 1)
        case Agent.MoveDown => this.copy(row = row - 1)
        case Agent.MoveDownRight => this.copy(row = row - 1, col = col + 1)

        case _ => this.copy()
      }
      if (updated.row >= 0 && updated.row <= ws.nRows && updated.col >= 0 && updated.col <= ws.nCols) {
        updated
      } else {
        this
      }
    }
  }

  case class StateContainer(
    world: scala.collection.mutable.Map[(Int, Int), List[GameEntity]] = scala.collection.mutable.Map[(Int, Int), List[World.GameEntity]]())
  case class Size(nRows: Int, nCols: Int)

  // TODO
  //case class Configuration(plantProbability: Double)

  def props(teams: Iterable[Game.Team],
    worldSize: Size,
    gameUpdateInterval: FiniteDuration) =
    Props(new World(teams, worldSize, gameUpdateInterval))
}

class World(teams: Iterable[Game.Team], worldSize: World.Size, gameUpdateInterval: FiniteDuration) extends Actor with ActorLogging {
  import context.dispatcher

  context.system.eventStream.subscribe(self, classOf[World.UpdateEntity])
  context.system.eventStream.subscribe(self, classOf[World.RemoveEntity])

  // announce the worlds state to everyone at a fixed interval
  val scheduler = context.system.scheduler.schedule(0 seconds, gameUpdateInterval, self, World.AnnounceWorldState)

  // empty state
  var state = World.StateContainer()
  var IDCounter: Long = 0

  def getUniqueID() = {
    IDCounter += 1
    IDCounter
  }

  def announceState() = context.system.eventStream.publish(state)

  override def postStop() = {
    context.system.eventStream.unsubscribe(self)
    scheduler.cancel
  }

  /** initializes the world state **/
  override def preStart() = {
    for {
      i <- 0 to worldSize.nRows
      j <- 0 to worldSize.nCols
    } {
      state = World.StateContainer(state.world += (i, j) -> List.empty[World.GameEntity])

      // add plant?
      if (scala.util.Random.nextBoolean) {
        val plantEntity = World.PlantEntity(id = getUniqueID, energy = 1.0, position = World.Location(row = i, col = j), selfRef = self)
        context.actorOf(Plant.props(plantEntity, gameUpdateInterval))
      }
    }

    teams.foreach {
      team =>
        val startRow = scala.util.Random.shuffle(0 to worldSize.nRows - 1).head
        val startCol = scala.util.Random.shuffle(0 to worldSize.nCols - 1).head
        val teamStartLocation = World.Location(startRow, startCol)
        for (i <- 1 to 25) {
          val entity = World.AgentEntity(
            position = teamStartLocation,
            id = getUniqueID,
            team = team.id,
            health = 1.0,
            selfRef = self,
            world = self)
          context.actorOf(Agent.props(entity, team.behaviour, worldSize, gameUpdateInterval))
        }
    }
  }

  //  def removeElement(entity: World.GameEntity, list: List[World.GameEntity]) = list diff List(entity)

  /** method to remove an entity from the world **/
  def remove(entity: World.GameEntity) = {
    state = World.StateContainer {
      state.world.map {
        case ((row, col), entities) => ((row, col), entities.filterNot(_.id == entity.id))
      }
    }
  }

  /** update method to update an entity in the world **/
  def update(entity: World.GameEntity) = {
    remove(entity)
    val (r, c) = (entity.position.row, entity.position.col)
    val updated = state.world((r, c)) ++ List(entity)
    World.StateContainer(state.world += (r, c) -> updated)
  }

  def receive = {
    case World.AnnounceWorldState => announceState
    case World.GetUniqueID => sender ! World.UniqueID(getUniqueID)
    case World.RemoveEntity(entity) => remove(entity)
    case World.UpdateEntity(entity) => update(entity)
  }
}
