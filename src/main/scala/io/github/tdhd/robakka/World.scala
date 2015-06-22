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
  def props(worldSize: Size): Props = Props(new World(worldSize))
}

class World(worldSize: Size) extends Actor with ActorLogging {
  import context.dispatcher

  context.system.eventStream.subscribe(self, classOf[AgentState])
  context.system.eventStream.subscribe(self, classOf[AgentDeath])

  // announce the worlds state to everyone at a fixed interval
  val scheduler = context.system.scheduler.schedule(0 seconds, 500 milliseconds)(announceState)
  // TODO: the state of the world should contain more than just the agent state
  // also plants for example
  var state = Map.empty[Long, AgentState]
  def announceState() = context.system.eventStream.publish(WorldState(state))

  override def postStop() = {
    context.system.eventStream.unsubscribe(self)
    scheduler.cancel
  }

  // spawns initial Agents
  override def preStart() = {
    for (i <- 1 to 25) {
      context.watch(context.actorOf(Agent.props()))
    }
  }

  def receive = {
    case AgentDeath(id) =>
      state -= id
    case AgentState(id, location, team, health, ref) =>
      if (state.contains(id)) {
        state -= id
      }
      state += (id -> AgentState(id, location, team, health, ref))
  }
}
