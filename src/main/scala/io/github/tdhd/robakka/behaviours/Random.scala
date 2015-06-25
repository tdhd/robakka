package io.github.tdhd.robakka.behaviours

import io.github.tdhd.robakka._

case object RandomBehaviour extends BaseBehaviour {
  def act(entity: World.AgentEntity, worldState: World.State) = {
    val move = (scala.util.Random.nextBoolean, scala.util.Random.nextBoolean) match {
      case (true, true) => Agent.MoveUp
      case (false, true) => Agent.MoveDown
      case (true, false) => Agent.MoveLeft
      case (false, false) => Agent.MoveRight
    }

    val enemies = worldState.entities.filter {
      case World.AgentEntity(World.Location(row, col), id, team, health, ref, world) => id != entity.agentId && team != entity.team
      case _ => false
    }.asInstanceOf[List[World.AgentEntity]]

    if (enemies.isEmpty) {
      Agent.CommandSet(move = Option(move))
    } else {
      Agent.CommandSet(move = Option(move), action = Option(Agent.Shoot(enemies.head.selfRef)))
    }
  }
}
