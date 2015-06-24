package io.github.tdhd.robakka.behaviours

import io.github.tdhd.robakka._

case object RandomBehaviour extends BaseBehaviour {
  def act(entity: AgentEntity, worldState: WorldState) = {
    val move = (scala.util.Random.nextBoolean, scala.util.Random.nextBoolean) match {
      case (true, true) => MoveUp
      case (false, true) => MoveDown
      case (true, false) => MoveLeft
      case (false, false) => MoveRight
    }

    val enemies = worldState.entities.filter {
      case AgentEntity(GridLocation(row, col), id, team, health, ref, world) => id != entity.agentId && team != entity.team
      case _ => false
    }.asInstanceOf[List[AgentEntity]]

    if (enemies.isEmpty) {
      CommandSet(move = Option(move))
    } else {
      CommandSet(move = Option(move), action = Option(Shoot(enemies.head.selfRef)))
    }
  }
}
