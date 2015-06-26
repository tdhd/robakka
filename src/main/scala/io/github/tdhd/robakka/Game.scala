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

import io.github.tdhd.robakka.behaviours.BaseBehaviour

object Game {
  case class Subscribe(ref: ActorRef)
  case class Unsubscribe(ref: ActorRef)

  case class Team(id: Long, behaviour: BaseBehaviour)

  def props(teams: Iterable[Game.Team], worldSize: World.Size = World.Size(30, 30)) = Props(new Game(teams, worldSize))
}

/**
 * creates a world with (0 to 30) rows and cols
 */
class Game(teams: Iterable[Game.Team], worldSize: World.Size = World.Size(30, 30)) extends Actor with ActorLogging {
  import context.dispatcher

  // create the game world
  val world = context.watch(context.actorOf(World.props(teams, worldSize), "world"))
  // a list of subscribers which wish to be notified about the game
  var gameSubscribers = List.empty[ActorRef]
  // subscribe to world states
  context.system.eventStream.subscribe(self, classOf[World.State])

  def die() = {
    context.system.eventStream.unsubscribe(self)
    context.stop(world)
    context.stop(self)
  }

  def receive = {
    case Game.Subscribe(ref) => gameSubscribers +:= ref
    case Game.Unsubscribe(ref) => gameSubscribers = gameSubscribers.filterNot(_ == ref)
    case ws: World.State => gameSubscribers.foreach(_ ! ws)

    case Terminated(ref) =>
      log.info("{} terminated, {} children left", ref, context.children.size)
      die()
  }
}
