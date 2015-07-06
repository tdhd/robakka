package io.github.tdhd.robakka.behaviours

import io.github.tdhd.robakka._

case object RandomBehaviour extends BaseBehaviour {
  def act(entity: World.AgentEntity, worldState: World.StateContainer) = {

    val move = BehaviourHelpers.getRandomMove
    val agents = BehaviourHelpers.entities2MoveCommand(entity, worldState)
    val enemies = agents.filter {
      case (World.AgentEntity(_, agentId, team, health, _, _), _) => team != entity.team
      case _ => false
    }
    val shooting = enemies.collect {
      case (World.AgentEntity(_, _, _, _, ref, _), _) => Agent.Shoot(ref)
    }.headOption

    Agent.CommandSet(move = Option(move), action = shooting)
  }
}
