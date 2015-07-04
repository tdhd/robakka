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

object World {
  // world -> world
  case object AnnounceWorldState
  // agent -> world
  case object GetUniqueAgentID
  // world -> agent
  case class UniqueAgentID(id: Long)
  // agent -> world
  case class UpdateAgent(agent: AgentEntity)
  // agent -> world
  case class RemoveAgent(agent: AgentEntity)
  // plant -> world
  case class UpdatePlant(plant: PlantEntity)
  // plant -> world
  case class RemovePlant(plant: PlantEntity)
  // agent -> plant
  case class ConsumePlant(plant: PlantEntity, by: AgentEntity)
  // plant -> agent
  case class PlantConsumed(energy: Double)

  // elements of the game
  sealed trait GameEntity {
    def position: Location
    // TODO: unify with all entities (agents and plants)
    //def id: Long
  }
  case class AgentEntity(position: Location,
    agentId: Long,
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

  context.system.eventStream.subscribe(self, classOf[World.UpdateAgent])
  context.system.eventStream.subscribe(self, classOf[World.RemoveAgent])
  context.system.eventStream.subscribe(self, classOf[World.UpdatePlant])
  context.system.eventStream.subscribe(self, classOf[World.RemovePlant])

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
            agentId = getUniqueID,
            team = team.id,
            health = 1.0,
            selfRef = self,
            world = self)
          context.actorOf(Agent.props(entity, team.behaviour, worldSize, gameUpdateInterval))
        }
    }
  }

  def removeAgent(agent: World.AgentEntity) = {
    state = World.State {
      state.entities.filterNot {
        case World.AgentEntity(_, id, _, _, _, _) => id == agent.agentId
        case _ => false
      }
    }
  }

  def updateAgent(entity: World.AgentEntity) = {
    removeAgent(entity)
    state = World.State { state.entities :+ entity }
  }

  def removePlant(plant: World.PlantEntity) = {
    state = World.State {
      state.entities.filterNot {
        case World.PlantEntity(id, _, _, _) => id == plant.id
        case _ => false
      }
    }
  }
  def updatePlant(entity: World.PlantEntity) = {
    removePlant(entity)
    state = World.State { state.entities :+ entity }
  }
  def receive = {
    case World.AnnounceWorldState => announceState
    case World.GetUniqueAgentID => sender ! World.UniqueAgentID(getUniqueID)
    case World.RemoveAgent(agent) => removeAgent(agent)
    case World.UpdateAgent(agent) => updateAgent(agent)
    case World.UpdatePlant(plant) => updatePlant(plant)
    case World.RemovePlant(plant) => removePlant(plant)
  }
}
