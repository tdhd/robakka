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

case class Size(nRows: Int, nCols: Int)

object World {
  def props(teams: Iterable[GameTeam], worldSize: Size) = Props(new World(teams, worldSize))
}

class World(teams: Iterable[GameTeam], worldSize: Size) extends Actor with ActorLogging {
  import context.dispatcher

  context.system.eventStream.subscribe(self, classOf[AgentEntity])
  context.system.eventStream.subscribe(self, classOf[RemoveAgent])

  // announce the worlds state to everyone at a fixed interval
  val scheduler = context.system.scheduler.schedule(0 seconds, 1000 milliseconds, self, AnnounceWorldState)

  var state = WorldState(entities = List.empty[GameEntity])
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
        val plantEntity = PlantEntity(position = GridLocation(row = i, col = j))
        state = WorldState { state.entities :+ plantEntity }
      }
    }

    teams.foreach {
      team =>
        val teamStartLocation = GridLocation(scala.util.Random.nextInt(30), scala.util.Random.nextInt(60))
        for (i <- 1 to 25) {
          val entity = AgentEntity(
            position = teamStartLocation,
            agentId = getUniqueAgentID,
            team = team.id,
            health = 1.0,
            selfRef = self,
            world = self)
          context.actorOf(Agent.props(entity, team.behaviour, worldSize))
        }
    }
  }

  def removeAgent(agent: AgentEntity) = {
    state = WorldState {
      state.entities.filterNot {
        case AgentEntity(_, id, _, _, _, _) => id == agent.agentId
        case _ => false
      }
    }
  }

  def removePlant(location: GridLocation) = {
    state = WorldState {
      state.entities.filterNot {
        case PlantEntity(GridLocation(row, col)) => location.row == row && location.col == col
        case _ => false
      }
    }
  }

  def addAgent(entity: AgentEntity) = {
    removeAgent(entity)
    state = WorldState { state.entities :+ entity }
  }

  def receive = {
    case AnnounceWorldState => announceState
    case GetUniqueAgentID => sender ! UniqueAgentID(getUniqueAgentID)

    case RemovePlant(location) => removePlant(location)
    case RemoveAgent(agent) => removeAgent(agent)
    case agentEntity: AgentEntity => addAgent(agentEntity)
  }
}
