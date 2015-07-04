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
import io.github.tdhd.robakka.behaviours._

object Plant {
  case object PlantSelfAction
  def props(entity: World.PlantEntity, gameUpdateInterval: FiniteDuration) =
    Props(new Plant(entity, gameUpdateInterval))
}

/**
 * the main plant class
 */
class Plant(entity: World.PlantEntity, gameUpdateInterval: FiniteDuration) extends Actor with ActorLogging {
  import context.dispatcher

  val scheduler = context.system.scheduler.schedule(0 seconds, gameUpdateInterval, self, Plant.PlantSelfAction)

  val selfState = entity.copy(selfRef = self)

  def die() = {
    scheduler.cancel
    context.system.eventStream.publish(World.RemoveEntity(selfState))
    context.stop(self)
  }

  def update() = {
    // publish own state
    context.system.eventStream.publish(World.UpdateEntity(selfState))
  }

  def receive = {
    case Plant.PlantSelfAction => update()
    case World.ConsumePlant =>
      sender ! World.PlantConsumed(selfState.energy)
      die()
    case _ =>
  }
}
