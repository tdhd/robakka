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
  def props(worldSize: Size) = Props(new World(worldSize))
}

class World(worldSize: Size) extends Actor with ActorLogging {
  import context.dispatcher

  context.system.eventStream.subscribe(self, classOf[AgentEntity])
  context.system.eventStream.subscribe(self, classOf[AgentDeath])

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
    for{i <- 1 to worldSize.nRows
    	j <- 1 to worldSize.nCols} {
    	  val grassEntity = GrassEntity(position = GridLocation(row = i, col = j))
    	  state = WorldState{state.entities ++ List(grassEntity)}
    	}

    for (i <- 1 to 25) {
      val entity = AgentEntity(
          position = GridLocation(scala.util.Random.nextInt(30), scala.util.Random.nextInt(60)),
          agentId = getUniqueAgentID,
          team = scala.util.Random.nextBoolean,
          health = 1.0,
          ref = self)
      //      TODO: load behaviour from command line
      val behaviour = io.github.tdhd.robakka.behaviours.SameRowBehaviour
      //      val behaviour = io.github.tdhd.robakka.behaviours.RandomBehaviour

      context.actorOf(Agent.props(entity, behaviour))
    }
  }

  def removeAgentFromWorld(agentId: Long) = {
      state = WorldState {
        state.entities.filterNot {
          case AgentEntity(_, id, _, _, _) => id == agentId
          case _ => false
        }
      }
  }

  def addAgentToWorld(entity: AgentEntity) = state = WorldState { state.entities :+ entity }

  def receive = {
    case AnnounceWorldState => announceState
    case GetUniqueAgentID => sender ! UniqueAgentID(getUniqueAgentID)

    case AgentDeath(agent) => removeAgentFromWorld(agent.agentId)
    case agentEntity: AgentEntity =>
      removeAgentFromWorld(agentEntity.agentId)
      addAgentToWorld(agentEntity)
  }
}
