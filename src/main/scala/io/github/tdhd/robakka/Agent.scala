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
  def props(entity: AgentEntity, behaviour: BaseBehaviour, worldSize: Size) = {
    Props(new Agent(entity, behaviour, worldSize))
  }
}

class Agent(entity: AgentEntity, behaviour: BaseBehaviour, worldSize: Size) extends Actor with ActorLogging {
  import context.dispatcher
  // for ? pattern
  implicit val timeout = Timeout(1 seconds)

  // subscribe to changes of the world
  context.system.eventStream.subscribe(self, classOf[WorldState])

  // TODO: aggressive, defensive
  // val stance = false
  // update reference: if not copied, the ref of the world is kept
  var selfState = entity.copy(selfRef = self)

  var worldState = WorldState(entities = List.empty[GameEntity])

  // schedule messages to self
  val scheduler = context.system.scheduler.schedule(0 seconds, 1000 milliseconds, self, AgentSelfAction)

  override def postStop() = context.system.eventStream.unsubscribe(self)

  def die() = {
    scheduler.cancel
    context.system.eventStream.publish(AgentDeath(selfState))
    context.stop(self)
  }

  // takes this.worldState and filters it accordingly to the neighbourhood of the agent
  def localWorldState() = {
    WorldState {
      worldState.entities.filter {
        case entity: GameEntity =>
          scala.math.abs(entity.position.row - selfState.position.row) <= 1 && scala.math.abs(entity.position.col - selfState.position.col) <= 1
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
      (selfState.world ? GetUniqueAgentID).mapTo[UniqueAgentID].onSuccess {
        case UniqueAgentID(spawnId) =>
          // create copy of self and spawn child, reduce own health
          val agentEntity = selfState.copy(agentId = spawnId, health = newHealth)
          context.actorOf(Agent.props(agentEntity, behaviour, worldSize))
          selfState = selfState.copy(health = newHealth)
      }
    }
  }

  def printAgentWorldState() = {
    localWorldState.entities.foreach {
      case GrassEntity(GridLocation(row, col)) => println(s"grass at ($row, $col")
      case AgentEntity(GridLocation(row, col), _, _, _, _, _) => println(s"agent at ($row, $col)")
      case _ =>
    }
  }
  /**
   * regenerate health and clip and maximum health
   */
  def regenHealth() = {
    selfState = selfState.health + scala.util.Random.nextDouble match {
      case i if i > 1.0 => selfState.copy(health = 1.0)
      case i => selfState.copy(health = i)
    }
  }

  /**
   * main routine for agent
   *
   * the agent behaviour returns a list of commands the agents then follows
   */
  def action() = {
    //    regenHealth()
    //    spawnChild()

    val commands = behaviour.act(selfState, localWorldState)

    // filter number of shoots down to 1
    commands.foreach {
      case MoveUp if selfState.position.row > 1 => selfState = selfState.copy(position = GridLocation(selfState.position.row - 1, selfState.position.col))
      case MoveDown if selfState.position.row < worldSize.nRows => selfState = selfState.copy(position = GridLocation(selfState.position.row + 1, selfState.position.col))
      case MoveLeft if selfState.position.col > 1 => selfState = selfState.copy(position = GridLocation(selfState.position.row, selfState.position.col - 1))
      case MoveRight if selfState.position.row < worldSize.nCols => selfState = selfState.copy(position = GridLocation(selfState.position.row, selfState.position.col + 1))
      case Shoot(ref) => ref ! Attack
      case _ =>
    }

    // publish own state
    context.system.eventStream.publish(selfState)
  }

  /**
   * TODO:
   * implement a chance to defend
   * implement an amount of damage to be taken
   * if defending -> more likely to not take damage
   */
  def defend() = {
    selfState = selfState.copy(health = selfState.health - scala.util.Random.nextDouble)
    if (selfState.health <= 0.0) {
      die()
    }
  }

  def receive = {
    case AgentSelfAction => action()
    case ws: WorldState => worldState = ws
    case Attack => defend()
  }
}
