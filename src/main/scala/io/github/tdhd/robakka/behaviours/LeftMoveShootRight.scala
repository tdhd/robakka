package io.github.tdhd.robakka.behaviours

import io.github.tdhd.robakka._

case object LeftMoveShootRightBehaviour extends BaseBehaviour {
  def act(entity: World.AgentEntity, worldState: World.State) = {

    val enemiesToRight = worldState.entities.filter {
      case World.AgentEntity(World.Location(row, col), id, team, health, ref, world) =>
        id != entity.agentId && team != entity.team && row == entity.position.row && entity.position.col == col - 1
      case _ => false
    }.asInstanceOf[List[World.AgentEntity]]

    if (enemiesToRight.isEmpty) {
      Agent.CommandSet(move = Option(Agent.MoveLeft))
    } else {
      Agent.CommandSet(move = Option(Agent.MoveLeft), action = Option(Agent.Shoot(enemiesToRight.head.selfRef)))
    }
  }
}
