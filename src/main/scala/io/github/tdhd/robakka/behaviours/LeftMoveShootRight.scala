package io.github.tdhd.robakka.behaviours

import io.github.tdhd.robakka._

case object LeftMoveShootRightBehaviour extends BaseBehaviour {
  def act(entity: World.AgentEntity, worldState: World.State) = {

    val enemiesToRight = BehaviourHelpers.getEnemies(entity, worldState).filter {
      case World.AgentEntity(World.Location(row, col), _, _, _, _, _) => row == entity.position.row && entity.position.col == col - 1
    }

    if (enemiesToRight.isEmpty) {
      Agent.CommandSet(move = Option(Agent.MoveLeft))
    } else {
      Agent.CommandSet(move = Option(Agent.MoveLeft), action = Option(Agent.Shoot(enemiesToRight.head.selfRef)))
    }
  }
}
