package io.github.tdhd.robakka.behaviours

import io.github.tdhd.robakka._
import io.github.tdhd.robakka.Agent.MoveCommand

case object FollowEnemyBehaviour extends BaseBehaviour {
  def act(entity: World.AgentEntity, worldState: World.State) = {

    val agents = BehaviourHelpers.entities2MoveCommand[World.AgentEntity](entity, worldState)

    val enemies = agents.filter {
      case (World.AgentEntity(_, agentId, team, health, _, _), _) => team != entity.team
    }

    val shootings: List[Option[Agent.ActionCommand]] = enemies.take(1).map {
      case (World.AgentEntity(_, _, _, _, ref, _), _) => Option(Agent.Shoot(ref))
    }

    //    shootings.filter(_.isEmpty)

    val move: Option[Agent.MoveCommand] = if (enemies.isEmpty) {
      Option(BehaviourHelpers.getRandomMove)
    } else {
      Option(enemies.head._2)
    }

    if (shootings.isEmpty) {
      Agent.CommandSet(move = move)
    } else {
      Agent.CommandSet(move = move, action = shootings.head)
    }
  }
}

