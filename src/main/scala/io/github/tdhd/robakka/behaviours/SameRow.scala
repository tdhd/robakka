package io.github.tdhd.robakka.behaviours

import io.github.tdhd.robakka.GridLocation
import io.github.tdhd.robakka.AgentState

case class SameRowBehaviour(location: GridLocation, worldState: Map[Long, AgentState]) extends BaseBehaviour {
  // TODO: pass as argument
  val id = 1
  val team = false

  def act() = {
    val enemiesOnSameRow = worldState.filter {
      case (id, AgentState(_, team, GridLocation(row, col), ref)) =>
        id != this.id && team != this.team && row == this.location.row
    }

    val teamOnSameRow = !worldState.filter {
      case (id, AgentState(_, team, GridLocation(row, col), ref)) =>
        id != this.id && team == this.team && row == this.location.row
    }.isEmpty

    if (teamOnSameRow) {
      if (scala.util.Random.nextBoolean) {
        GridLocation(location.row, location.col - 1)
      } else {
        GridLocation(location.row, location.col + 1)
      }
    } else {
      if (scala.util.Random.nextBoolean) {
        GridLocation(location.row - 1, location.col)
      } else {
        GridLocation(location.row + 1, location.col)
      }
    }
  }
}

