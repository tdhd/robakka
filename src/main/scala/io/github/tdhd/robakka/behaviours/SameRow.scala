package io.github.tdhd.robakka.behaviours

import io.github.tdhd.robakka._

case object SameRowBehaviour extends BaseBehaviour {
  def act(entity: AgentEntity, worldState: WorldState) = {
    val enemiesOnSameRow = worldState.entities.filter {
      case AgentEntity(GridLocation(row, col), id, team, health, ref, world) =>
        id != entity.agentId && team != entity.team && row == entity.position.row
      case _ => false
    }

    val shootings: List[ActionCommand] = enemiesOnSameRow.map {
      case AgentEntity(_, _, _, _, ref, _) => Shoot(ref)
    }

    // movement depends on whether on the same row there is a teammate
    val teamOnSameRow = !worldState.entities.filter {
      case AgentEntity(GridLocation(row, col), id, team, health, ref, world) =>
        id != entity.agentId && team == entity.team && row == entity.position.row
      case _ => false
    }.isEmpty

    val move: MoveCommand = if (teamOnSameRow) {
      if (scala.util.Random.nextBoolean) {
        MoveLeft
      } else {
        MoveRight
      }
    } else {
      if (scala.util.Random.nextBoolean) {
        MoveDown
      } else {
        MoveUp
      }
    }

    if (shootings.isEmpty) {
      CommandSet(move = Option(move))
    } else {
      CommandSet(move = Option(move), action = Option(shootings.head))
    }
  }
}

