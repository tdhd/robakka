package io.github.tdhd.robakka.behaviours

import io.github.tdhd.robakka._

case object SameRowBehaviour extends BaseBehaviour {
  def act(entity: AgentEntity, worldState: WorldState) = {
    val enemiesOnSameRow = worldState.entities.filter {
      case AgentEntity(GridLocation(row, col), id, team, health, ref) =>
        id != entity.agentId && team != entity.team && row == entity.position.row
      case _ => false
    }

    val teamOnSameRow = !worldState.entities.filter {
      case AgentEntity(GridLocation(row, col), id, team, health, ref) =>
        id != entity.agentId && team == entity.team && row == entity.position.row
      case _ => false
    }.isEmpty

    val shootings: List[AgentCommand] = enemiesOnSameRow.map {
      case AgentEntity(_, _, _, _, ref) => Shoot(ref)
    }.toList

    val res: AgentCommand = if (teamOnSameRow) {
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

    List(res) ++ shootings
  }
}

