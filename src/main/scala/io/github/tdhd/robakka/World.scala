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
  def props(): Props = Props(new World())
}

class World extends Actor with ActorLogging {
  import context.dispatcher
  // needed for `?` below
//  implicit val timeout = Timeout(1 seconds)

  // spawns initial Agents
  override def preStart() = {
    for (i <- 1 to 10) {
      context.watch(context.actorOf(Agent.props()))
    }
  }

  def receive = {
    case msg =>
      //println(msg)
  }
}
