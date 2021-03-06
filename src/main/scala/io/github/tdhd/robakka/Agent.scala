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
  // agent -> agent
//  case object AgentSelfAction
  // agent -> agent
  case class Attack(damage: Double)

  //  object Commands {
  //    object Move {
  //      case object Left
  //    }
  //    object Action {
  //      case class Shoot(who: ActorRef)
  //    }
  //  }

  sealed trait AgentCommand
  sealed trait MoveCommand extends AgentCommand
  case object MoveUpLeft extends MoveCommand
  case object MoveUp extends MoveCommand
  case object MoveUpRight extends MoveCommand
  case object MoveLeft extends MoveCommand
  case object MoveRight extends MoveCommand
  case object MoveDownLeft extends MoveCommand
  case object MoveDown extends MoveCommand
  case object MoveDownRight extends MoveCommand

  sealed trait ActionCommand extends AgentCommand
  case class Shoot(who: ActorRef) extends ActionCommand
  //sealed trait StanceCommand extends AgentCommand
  //case object Defensive extends StanceCommand
  //case object Aggressive extends StanceCommand
  // defines the set of command an agent can issue at one given point in time
  case class CommandSet(
    move: Option[MoveCommand] = Option.empty[MoveCommand],
    action: Option[ActionCommand] = Option.empty[ActionCommand])

  def props(entity: World.AgentEntity,
    behaviour: BaseBehaviour,
    worldSize: World.Size,
    gameUpdateInterval: FiniteDuration) =
    Props(new Agent(entity, behaviour, worldSize, gameUpdateInterval))
}

/**
 * the main agent class
 */
class Agent(entity: World.AgentEntity, behaviour: BaseBehaviour, worldSize: World.Size, gameUpdateInterval: FiniteDuration) extends Actor with ActorLogging {
  import context.dispatcher
  // for ? pattern
  implicit val timeout = Timeout(1 seconds)

  // schedule messages to self
//  val scheduler = context.system.scheduler.schedule(0 seconds, gameUpdateInterval, self, Agent.AgentSelfAction)
  // subscribe to changes of the world
  context.system.eventStream.subscribe(self, classOf[World.StateContainer])

  // update reference: if not copied, the ref of the world is kept
  var selfState = entity.copy(selfRef = self)
  var worldState = World.StateContainer()

  override def postStop() = context.system.eventStream.unsubscribe(self)

  /**
   * returns the neighbourhood of the current position
   */
  def localWorldState() = {
    World.StateContainer {
      worldState.world.filter {
        case ((row, col), _) => scala.math.abs(row - selfState.position.row) <= 1 && scala.math.abs(col - selfState.position.col) <= 1
      }
    }
  }

  /**
   * Spawn a child when this.health > lowerHealthThreshold
   * and split health evenly between this and child
   */
  def spawnChild(lowerHealthThreshold: Double = 0.75, healthReductionFactor: Double = 2.0) = {
    if (selfState.health > lowerHealthThreshold) {
      val newHealth = selfState.health / healthReductionFactor
      (selfState.world ? World.GetUniqueID).mapTo[World.UniqueID].onSuccess {
        case World.UniqueID(spawnId) =>
          // create copy of self and spawn child, reduce own health
          val agentEntity = selfState.copy(id = spawnId, health = newHealth)
          context.actorOf(Agent.props(agentEntity, behaviour, worldSize, gameUpdateInterval))
          selfState = selfState.copy(health = newHealth)
      }
    }
  }

  /**
   * update health and clip at max
   */
  def updateHealth(newHealth: Double) = {
    selfState = newHealth match {
      case h if h > 1.0 => selfState.copy(health = 1.0)
      case h => selfState.copy(health = h)
    }
  }

  /**
   * regenerate health and clip and maximum health
   */
  def regenHealth() = updateHealth(selfState.health + scala.util.Random.nextDouble)

  /**
   * consumes a plant if standing on it
   */
  def consumePlant() = {
    val somePlant = worldState.world.flatMap {
      case ((row, col), entities) => entities.filter {
        case World.PlantEntity(World.Location(row, col), _, _, _) => row == selfState.position.row && col == selfState.position.col
        case _ => false
      }
    }.headOption.asInstanceOf[Option[World.PlantEntity]]

    somePlant.foreach {
      p =>
        // ask the plant to be consumed and update health in case of success
        (p.selfRef ? World.ConsumePlant).mapTo[World.PlantConsumed].onSuccess {
          case World.PlantConsumed(energy) => updateHealth(selfState.health + energy)
        }
    }
  }

  def move(c: Agent.CommandSet) = {
    // either translate or return current position in case the move option was not defined
    val updatedPosition = c.move.map(selfState.position.translate(_, worldSize)).getOrElse(selfState.position)
    selfState = selfState.copy(position = updatedPosition)
  }

  def action(c: Agent.CommandSet) = {
    val attackDamage = scala.util.Random.nextDouble
    c match {
      case Agent.CommandSet(_, Some(Agent.Shoot(ref))) => ref ! Agent.Attack(attackDamage)
      case _ =>
    }
  }

  /**
   * main routine for agent
   *
   * - regenerate health
   * - spawn child
   * - move
   * - consume plant (gain health)
   * - take action
   */
  def act() = {
    //regenHealth()
    //spawnChild()

    val commandSet = behaviour.act(selfState, localWorldState)
    move(commandSet)
    consumePlant()
    action(commandSet)

    // publish own state
    context.system.eventStream.publish(World.UpdateEntity(selfState))
  }

  /**
   * defend against incoming attack
   */
  def defend(damage: Double) = {
    selfState = selfState.copy(health = selfState.health - damage)
    if (selfState.health <= 0.0) {
      die()
    }
  }

  def die() = {
//    scheduler.cancel
    context.system.eventStream.publish(World.RemoveEntity(selfState))
    context.stop(self)
  }

  def receive = {
    // TODO
    //case Agent.AgentSelfAction => act()
    case sc: World.StateContainer =>
      worldState = sc
      act()
    case Agent.Attack(damage) => defend(damage)
    case _ =>
  }
}
