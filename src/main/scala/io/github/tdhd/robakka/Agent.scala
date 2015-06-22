package io.github.tdhd.robakka

import language.postfixOps
import scala.concurrent.duration._
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorLogging
import akka.actor.Props
import akka.actor.Terminated
import akka.actor.Cancellable
import akka.util.Timeout
import akka.pattern.{ ask, pipe }

object Agent {
  def props(): Props = Props(new Agent())
}

class Agent extends Actor with ActorLogging {
  import context.dispatcher
  implicit val timeout = Timeout(1 seconds)

  // subscribe to changes of the world
  context.system.eventStream.subscribe(self, classOf[WorldState])

  // TODO: ensure this is unique!
//  val id = scala.util.Random.nextLong
//  var location = 
  // TODO: make this Int
//  val team = scala.util.Random.nextBoolean
//  var health = 1.0
  // TODO: aggressive, defensive
  // val stance = false
  var selfState = AgentState(
      scala.util.Random.nextLong,
      GridLocation(scala.util.Random.nextInt(30), scala.util.Random.nextInt(60)),
      scala.util.Random.nextBoolean,
      1.0, self)

  val world = context.parent
  var worldState = Map.empty[Long, AgentState]

  // scheduler for the action of an agent
  val scheduler = context.system.scheduler.schedule(0 seconds, 250 milliseconds)(action)

  def die() = context.system.eventStream.publish(AgentDeath(selfState.id))

  override def postStop() = {
    context.system.eventStream.unsubscribe(self)
  }

  // takes this.worldState and filters it accordingly to the neighbourhood of the agent
  def localWorldState() = {
    worldState.filter{
      case (id, AgentState(_, GridLocation(row, col), team, health, ref)) =>
        id != selfState.id && scala.math.abs(row - selfState.location.row) <= 1 && scala.math.abs(col - selfState.location.col) <= 1
    }
  }

  /**
   * main routine for agent 
   */
  def action() = {

    // TODO: parse result of behaviour
    // - update location
    // - issue attack
    // - cannot issue defend, since this must be implemented in receive

//    val temp = io.github.tdhd.robakka.behaviours.RandomBehaviour(selfState, worldState).act()
    val temp = io.github.tdhd.robakka.behaviours.SameRowBehaviour(selfState, worldState).act()

    // update location
    selfState = selfState.copy(location = temp)

    // publish own state
    context.system.eventStream.publish(selfState)
  }

  def receive = {
    case WorldState(state) =>
      worldState = state
    case Attack =>
//      scheduler.cancel
//      context.stop(self)
      // TODO: context.stop(self) triggers exception

      // TODO: implement a chance to defend
      // TODO: implement an amount of damage to be taken
      // TODO: if defending -> more likely to not take damage
      die()
      context.system.eventStream.publish(AgentDeath(selfState.id))
      scheduler.cancel
  }
}
