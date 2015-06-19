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
  val id = scala.util.Random.nextLong
  var location = GridLocation(scala.util.Random.nextInt(30), scala.util.Random.nextInt(60))
  // TODO: make this Int
  val team = scala.util.Random.nextBoolean
  var health = 1.0

  val world = context.parent
  var worldState = Map.empty[Long, AgentState]

  // scheduler for the action of an agent
  val scheduler = context.system.scheduler.schedule(0 seconds, 250 milliseconds)(action)

  def die() = context.system.eventStream.publish(AgentDeath(id))

  override def postStop() = {
    context.system.eventStream.unsubscribe(self)
  }

  // takes this.worldState and filters it accordingly to the neighbourhood of the agent
  def localWorldState() = {
    worldState.filter{
      case (id, AgentState(_, team, GridLocation(row, col), ref)) =>
        id != this.id && scala.math.abs(row - location.row) <= 1 && scala.math.abs(col - location.col) <= 1
    }
  }

  /**
   * main routine for agent
   * at the moment this is a playground for different techniques
   */
  def action() = {
//    val teamOnSameRowLocal = ! localWorldState().filter{
//      case (id, AgentState(_, team, GridLocation(row, col), ref)) =>
//        id != this.id && team == this.team && row == this.location.row
//    }.isEmpty

    val enemiesOnSameRow = worldState.filter{
      case (id, AgentState(_, team, GridLocation(row, col), ref)) =>
        id != this.id && team != this.team && row == this.location.row
    }
    enemiesOnSameRow.foreach{
      case (id, AgentState(_, _, _, ref)) =>
        ref ! Attack
    }

    val teamOnSameRow = ! worldState.filter{
      case (id, AgentState(_, team, GridLocation(row, col), ref)) =>
        id != this.id && team == this.team && row == this.location.row
    }.isEmpty

    if (teamOnSameRow) {
      if(scala.util.Random.nextBoolean) {
        location = GridLocation(location.row, location.col-1)
      } else {
        location = GridLocation(location.row, location.col+1)
      }
    } else {
      if(scala.util.Random.nextBoolean) {
        location = GridLocation(location.row -1, location.col)
      } else {
        location = GridLocation(location.row + 1, location.col)
      }
    }

////     random mover
//    (scala.util.Random.nextBoolean, scala.util.Random.nextBoolean) match {
//      case (true, true) =>
//        location = GridLocation(location.row + 1, location.col - 1)
//      case (false, true) =>
//        location = GridLocation(location.row - 1, location.col - 1)
//      case (true, false) =>
//        location = GridLocation(location.row + 1, location.col + 1)
//      case (false, false) =>
//        location = GridLocation(location.row - 1, location.col + 1)
//    }

    // publish own state
    context.system.eventStream.publish(AgentState(id, team, location, self))
  }

  def receive = {
    case WorldState(state) =>
      worldState = state
    case Attack =>
//      scheduler.cancel
//      context.stop(self)
      // TODO: context.stop(self) triggers exception
      die()
      context.system.eventStream.publish(AgentDeath(id))
      scheduler.cancel
  }
}
