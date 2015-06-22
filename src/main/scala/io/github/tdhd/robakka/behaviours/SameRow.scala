package io.github.tdhd.robakka.behaviours

import io.github.tdhd.robakka.GridLocation
import io.github.tdhd.robakka.AgentState

case class SameRowBehaviour(selfState: AgentState, worldState: Map[Long, AgentState]) extends BaseBehaviour {
  def act() = {
    val enemiesOnSameRow = worldState.filter {
      case (id, AgentState(_, GridLocation(row, col), team, health, ref)) =>
        id != selfState.id && team != selfState.team && row == selfState.location.row
    }

    val teamOnSameRow = !worldState.filter {
      case (id, AgentState(_, GridLocation(row, col), team, health, ref)) =>
        id != selfState.id && team == selfState.team && row == selfState.location.row
    }.isEmpty

    val res = if (teamOnSameRow) {
      if (scala.util.Random.nextBoolean) {
        GridLocation(selfState.location.row, selfState.location.col - 1)
      } else {
        GridLocation(selfState.location.row, selfState.location.col + 1)
      }
    } else {
      if (scala.util.Random.nextBoolean) {
        GridLocation(selfState.location.row - 1, selfState.location.col)
      } else {
        GridLocation(selfState.location.row + 1, selfState.location.col)
      }
    }
    res
  }
}

