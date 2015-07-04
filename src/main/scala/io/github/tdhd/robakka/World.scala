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
  case object GetUniqueAgentID
  // world -> agent
  case class UniqueAgentID(id: Long)
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
  case class PlantEntity(id: Long,
    energy: Double,
    position: Location,
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

  case class State(entities: List[GameEntity])
  case class Size(nRows: Int, nCols: Int)

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

  var state = World.State(entities = List.empty[World.GameEntity])
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

  // spawns initial Agents
  override def preStart() = {
    // create grass everywhere
    for {
      i <- 1 to worldSize.nRows
      j <- 1 to worldSize.nCols
    } {
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

  /** generic method to remove an entity from the world **/
  def remove[T: ClassTag](entity: World.GameEntity) = {
    state = World.State {
      state.entities.filterNot {
        case item: T => item.id == entity.id
        case _ => false
      }
    }
  }

  /** generic update method to update an entity in the world **/
  def update(entity: World.GameEntity) = {
    remove[entity.type](entity)
    state = World.State { state.entities :+ entity }
  }

  def receive = {
    case World.AnnounceWorldState => announceState
    case World.GetUniqueAgentID => sender ! World.UniqueAgentID(getUniqueID)
    case World.RemoveEntity(entity) => remove[entity.type](entity)
    case World.UpdateEntity(entity) => update(entity)
  }
}
