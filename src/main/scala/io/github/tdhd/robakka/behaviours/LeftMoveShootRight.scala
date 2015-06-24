package io.github.tdhd.robakka.behaviours

import io.github.tdhd.robakka._

case object LeftMoveShootRightBehaviour extends BaseBehaviour {
  def act(entity: AgentEntity, worldState: WorldState) = {

    val enemiesToRight = worldState.entities.filter {
      case AgentEntity(GridLocation(row, col), id, team, health, ref, world) =>
        id != entity.agentId && team != entity.team && row == entity.position.row && entity.position.col == col - 1
      case _ => false
    }.asInstanceOf[List[AgentEntity]]

    if (enemiesToRight.isEmpty) {
      CommandSet(move = Option(MoveLeft))
    } else {
      CommandSet(move = Option(MoveLeft), action = Option(Shoot(enemiesToRight.head.selfRef)))
    }
  }
}
