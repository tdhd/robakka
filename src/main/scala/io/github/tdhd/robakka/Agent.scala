package io.github.tdhd.robakka

import language.postfixOps
import scala.concurrent.duration._
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorLogging
import akka.actor.Props
import akka.actor.Terminated
import akka.actor.Cancellable

object Agent {
  def props(): Props = Props(new Agent())
}

class Agent extends Actor with ActorLogging {
  import context.dispatcher

  // TODO: ensure this is unique!
  val id = scala.util.Random.nextLong
  var location = GridLocation(10, 5)
  // TODO: make this not binary, but Int
  val team = scala.util.Random.nextBoolean
  var health = 1.0

  // signal death
  override def postStop() = {
    context.system.eventStream.publish(AgentDeath(id))
  }

  // scheduler
  val scheduler: Cancellable = context.system.scheduler.schedule(1 seconds, 1 seconds)(act)

  def act() = {
    (scala.util.Random.nextBoolean, scala.util.Random.nextBoolean) match {
      case (true, true) =>
        location = GridLocation(location.x + 1, location.y - 1)
      case (false, true) =>
        location = GridLocation(location.x - 1, location.y - 1)
      case (true, false) =>
        location = GridLocation(location.x + 1, location.y + 1)
      case (false, false) =>
        location = GridLocation(location.x - 1, location.y + 1)
    }

    // public update
    context.system.eventStream.publish(AgentState(id, team, location))
    // just die randomnly
    health -= scala.util.Random.nextDouble
    if (health < 0.0) {
      scheduler.cancel
      context.stop(self)
    }
  }

  def receive = {
    case _ =>
      println("agent received unknown message")
  }
}
