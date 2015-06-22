package io.github.tdhd.robakka.behaviours

import io.github.tdhd.robakka.GridLocation
import io.github.tdhd.robakka.AgentState

case class RandomBehaviour(selfState: AgentState, worldState: Map[Long, AgentState]) extends BaseBehaviour {
  def act() = {
    // random mover
    val newLoc = (scala.util.Random.nextBoolean, scala.util.Random.nextBoolean) match {
      case (true, true) =>
        GridLocation(selfState.location.row + 1, selfState.location.col - 1)
      case (false, true) =>
        GridLocation(selfState.location.row - 1, selfState.location.col - 1)
      case (true, false) =>
        GridLocation(selfState.location.row + 1, selfState.location.col + 1)
      case (false, false) =>
        GridLocation(selfState.location.row - 1, selfState.location.col + 1)
    }
    newLoc
  }
}
