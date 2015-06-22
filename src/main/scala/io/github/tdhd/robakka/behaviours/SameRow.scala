package io.github.tdhd.robakka.behaviours

import io.github.tdhd.robakka._

case object SameRowBehaviour extends BaseBehaviour {
  def act(selfState: AgentState, worldState: Map[Long, AgentState]) = {
    val enemiesOnSameRow = worldState.filter {
      case (id, AgentState(_, GridLocation(row, col), team, health, ref)) =>
        id != selfState.id && team != selfState.team && row == selfState.location.row
    }

    val teamOnSameRow = !worldState.filter {
      case (id, AgentState(_, GridLocation(row, col), team, health, ref)) =>
        id != selfState.id && team == selfState.team && row == selfState.location.row
    }.isEmpty

    val shootings: List[AgentCommand] = enemiesOnSameRow.map {
      case (_, AgentState(_, _, _, _, ref)) => Shoot(ref)
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

