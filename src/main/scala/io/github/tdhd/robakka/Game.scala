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

object Game {
  def props(): Props = Props(new Game())
}

class Game extends Actor with ActorLogging {
  import context.dispatcher
//  implicit val timeout = Timeout(1 seconds)

  val world = context.watch(context.actorOf(World.props(), "world"))
  val visualizer = context.watch(context.actorOf(Visualizer.props(), "visualizer"))

//  override def preStart() = {}

  def receive = {
    case Terminated(ref) =>
      log.info("{} terminated, {} children left", ref, context.children.size)
      // TODO
      context.stop(world)
      context.stop(visualizer)
      context.stop(self)

//      if (context.children.size.equals(1)){
//        // stop the listening actor
//        context.children.foreach(context.stop)
//        log.info("Dying!")
//        context.stop(self)
//      }
  }
}
