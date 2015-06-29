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
  // agent -> world
  case class ConsumePlant(plant: PlantEntity, by: AgentEntity)
  // world -> agent
  case class PlantConsumed(health: Double)

  // elements of the game
  sealed trait GameEntity {
    def position: Location
  }
  case class AgentEntity(position: Location,
    agentId: Long,
    team: Long,
    health: Double,
    selfRef: ActorRef,
    world: ActorRef) extends GameEntity
  case class PlantEntity(position: Location) extends GameEntity

  case class Location(row: Int, col: Int)
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

  // announce the worlds state to everyone at a fixed interval
  val scheduler = context.system.scheduler.schedule(0 seconds, gameUpdateInterval, self, World.AnnounceWorldState)

  var state = World.State(entities = List.empty[World.GameEntity])
  var agentIDCounter: Long = 0

  def getUniqueAgentID() = {
    agentIDCounter += 1
    agentIDCounter
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
        val plantEntity = World.PlantEntity(position = World.Location(row = i, col = j))
        state = World.State { state.entities :+ plantEntity }
      }
    }

    teams.foreach {
      team =>
        val startRow = scala.util.Random.shuffle(0 to worldSize.nRows).head
        val startCol = scala.util.Random.shuffle(0 to worldSize.nCols).head
        val teamStartLocation = World.Location(startRow, startCol)
        for (i <- 1 to 25) {
          val entity = World.AgentEntity(
            position = teamStartLocation,
            agentId = getUniqueAgentID,
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

  def consumePlant(plant: World.PlantEntity) = {

    val nEntitiesPre = state.entities.size
    state = World.State {
      state.entities.filterNot {
        case World.PlantEntity(World.Location(row, col)) => plant.position.row == row && plant.position.col == col
        case _ => false
      }
    }
    val nEntitiesPost = state.entities.size

    // consume plant
    if (nEntitiesPost < nEntitiesPre) {
      sender ! World.PlantConsumed(0.5)
    }
  }

  def updateAgent(entity: World.AgentEntity) = {
    removeAgent(entity)
    state = World.State { state.entities :+ entity }
  }

  def receive = {
    case World.AnnounceWorldState => announceState
    case World.GetUniqueAgentID => sender ! World.UniqueAgentID(getUniqueAgentID)
    case World.ConsumePlant(plant, ref) => consumePlant(plant)
    case World.RemoveAgent(agent) => removeAgent(agent)
    case World.UpdateAgent(agent) => updateAgent(agent)
  }
}
