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
  def props(entity: AgentEntity, behaviour: BaseBehaviour) = {
    Props(new Agent(entity, behaviour))
  }
}

class Agent(entity: AgentEntity, behaviour: BaseBehaviour) extends Actor with ActorLogging {
  import context.dispatcher
  // for ? pattern
  implicit val timeout = Timeout(1 seconds)

  // subscribe to changes of the world
  context.system.eventStream.subscribe(self, classOf[WorldState])

  // TODO: aggressive, defensive
  // val stance = false
  // update reference: if not copied, the ref of the world is kept
  var selfState = entity.copy(ref = self)

  val world = context.parent
  var worldState = WorldState(entities = List.empty[GameEntity])

  // schedule messages to self
  val scheduler = context.system.scheduler.schedule(0 seconds, 1000 milliseconds, self, AgentSelfAction)

  override def postStop() = context.system.eventStream.unsubscribe(self)

  def die() = {
    scheduler.cancel
    context.system.eventStream.publish(AgentDeath(selfState.agentId))
    context.stop(self)
  }

  // takes this.worldState and filters it accordingly to the neighbourhood of the agent
  def localWorldState() = {
    worldState.entities.filter {
      case entity: GameEntity =>
        scala.math.abs(entity.position.row - selfState.position.row) <= 1 && scala.math.abs(entity.position.col - selfState.position.col) <= 1 
    }
  }

  /**
   * Spawn a child when this.health > lowerHealthThreshold
   * and split health evenly between this and child
   *
   * TODO: childs creating childs does not work, they don't have the correct reference to the world
   * -> forward messages from childs to this.world
   * -> OR pass reference to world in constructor
   */
  def spawnChild(lowerHealthThreshold: Double = 0.75, healthReductionFactor: Double = 2.0) = {
    if (selfState.health > lowerHealthThreshold) {
      val newHealth = selfState.health / healthReductionFactor
      (world ? GetUniqueAgentID).mapTo[UniqueAgentID].onSuccess {
        case UniqueAgentID(spawnId) =>
          val entity = AgentEntity(
            agentId = spawnId,
            position = GridLocation(scala.util.Random.nextInt(30), scala.util.Random.nextInt(60)),
            team = selfState.team,
            health = newHealth, ref = self)
          context.actorOf(Agent.props(entity, behaviour))
          selfState = selfState.copy(health = newHealth)
      }
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
    //        spawnChild()

    // TODO: limit visibility of the world = localWorldState
    val commands = behaviour.act(selfState, worldState)

    // filter number of shoots down to 1
    commands.foreach {
      // TODO: get world dimensions
      case MoveUp if selfState.position.row > 1 => selfState = selfState.copy(position = GridLocation(selfState.position.row - 1, selfState.position.col))
      case MoveDown if selfState.position.row < 30 => selfState = selfState.copy(position = GridLocation(selfState.position.row + 1, selfState.position.col))
      case MoveLeft if selfState.position.col > 1 => selfState = selfState.copy(position = GridLocation(selfState.position.row, selfState.position.col - 1))
      case MoveRight if selfState.position.row < 60 => selfState = selfState.copy(position = GridLocation(selfState.position.row, selfState.position.col + 1))
      case Shoot(ref) => ref ! Attack
      case _ =>
    }

    // TODO: publish AgentEntity
    // publish own state
    context.system.eventStream.publish(selfState)
  }

  def receive = {
    case AgentSelfAction => action()
    case ws: WorldState => worldState = ws
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
