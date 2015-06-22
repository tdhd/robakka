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

object Agent {
  def props(initialState: AgentState, behaviour: BaseBehaviour) = {
    Props(new Agent(initialState, behaviour))
  }
}

class Agent(initialState: AgentState, behaviour: BaseBehaviour) extends Actor with ActorLogging {
  import context.dispatcher
  // for ? pattern
  //  implicit val timeout = Timeout(1 seconds)

  // subscribe to changes of the world
  context.system.eventStream.subscribe(self, classOf[WorldState])

  // TODO: aggressive, defensive
  // val stance = false
  // update reference: if not copied, the ref of the world is kept
  var selfState = initialState.copy(ref = self)

  // val world = context.parent
  var worldState = Map.empty[Long, AgentState]

  // schedule messages to self
  val scheduler = context.system.scheduler.schedule(0 seconds, 1000 milliseconds, self, AgentSelfAction)

  override def postStop() = context.system.eventStream.unsubscribe(self)

  def die() = {
    scheduler.cancel
    context.system.eventStream.publish(AgentDeath(selfState.id))
    context.stop(self)
  }

  // takes this.worldState and filters it accordingly to the neighbourhood of the agent
  def localWorldState() = {
    worldState.filter {
      case (id, AgentState(_, GridLocation(row, col), team, _, _)) =>
        id != selfState.id && scala.math.abs(row - selfState.location.row) <= 1 && scala.math.abs(col - selfState.location.col) <= 1
    }
  }

  def spawnChild() = {
    // spawn child and reduce health by a factor of 2
    if (selfState.health > 0.75) {
      val newHealth = selfState.health / 2
      val initialState = AgentState(
        // TODO: ensure this is unique!
        id = scala.util.Random.nextLong,
        location = GridLocation(scala.util.Random.nextInt(30), scala.util.Random.nextInt(60)),
        team = selfState.team,
        health = newHealth, ref = self)
      context.actorOf(Agent.props(initialState, behaviour))
      selfState = selfState.copy(health = newHealth)
    }
  }

  def regenHealth() = {
    // TODO: regen health and clip at maxHealth
    selfState = selfState.copy(health = selfState.health + scala.util.Random.nextDouble)
  }
  /**
   * main routine for agent
   */
  def action() = {
    // - cannot issue defend, since this must be implemented in receive

//    regenHealth()
//    spawnChild()

    // TODO: limit visibility of the world = localWorldState
    val commands = behaviour.act(selfState, worldState)

    // filter number of shoots down to 1
    commands.foreach {
      // TODO: get world dimensions
      case MoveUp if selfState.location.row > 1 => selfState = selfState.copy(location = GridLocation(selfState.location.row - 1, selfState.location.col))
      case MoveDown if selfState.location.row < 30 => selfState = selfState.copy(location = GridLocation(selfState.location.row + 1, selfState.location.col))
      case MoveLeft if selfState.location.col > 1 => selfState = selfState.copy(location = GridLocation(selfState.location.row, selfState.location.col - 1))
      case MoveRight if selfState.location.row < 60 => selfState = selfState.copy(location = GridLocation(selfState.location.row, selfState.location.col + 1))
      case Shoot(ref) => ref ! Attack
      case _ =>
    }

    // publish own state
    context.system.eventStream.publish(selfState)
  }

  def receive = {
    case AgentSelfAction => action()
    case WorldState(state) =>
      worldState = state
    case Attack =>
      // TODO:
      // - implement a chance to defend
      // - implement an amount of damage to be taken
      // - if defending -> more likely to not take damage
      selfState = selfState.copy(health = selfState.health - scala.util.Random.nextDouble)
      if (selfState.health <= 0.0) {
        die()
      }
  }
}
