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

  def props(teams: Iterable[Game.Team],
    worldSize: World.Size = World.Size(30, 30),
    gameUpdateInterval: FiniteDuration = 500 milliseconds) = Props(new Game(teams, worldSize, gameUpdateInterval))
}

/**
 * the main game object containing the world
 *
 * TODO:
 * - add API to allow learning of behaviours
 *
 */
class Game(teams: Iterable[Game.Team], worldSize: World.Size, gameUpdateInterval: FiniteDuration) extends Actor with ActorLogging {
  import context.dispatcher

  // create the game world
  val world = context.watch(context.actorOf(World.props(teams, worldSize, gameUpdateInterval), "world"))
  // a list of subscribers which wish to be notified about the game
  var gameSubscribers = List.empty[ActorRef]
  // subscribe to world states
  context.system.eventStream.subscribe(self, classOf[World.StateContainer])

  def die() = {
    context.system.eventStream.unsubscribe(self)
    context.stop(world)
    context.stop(self)
  }

  def receive = {
    case Game.Subscribe(ref) => gameSubscribers +:= ref
    case Game.Unsubscribe(ref) => gameSubscribers = gameSubscribers.filterNot(_ == ref)
    case sc: World.StateContainer => gameSubscribers.foreach(_ ! sc)

    case Terminated(ref) =>
      log.info("{} terminated, {} children left", ref, context.children.size)
      die()
  }
}
