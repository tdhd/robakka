package io.github.tdhd.robakka.behaviours

import io.github.tdhd.robakka.GridLocation
import io.github.tdhd.robakka.AgentState

case class RandomBehaviour(location: GridLocation, worldState: Map[Long, AgentState]) extends BaseBehaviour {
  def act() = {
    //     random mover
    val newLoc = (scala.util.Random.nextBoolean, scala.util.Random.nextBoolean) match {
      case (true, true) =>
        GridLocation(location.row + 1, location.col - 1)
      case (false, true) =>
        GridLocation(location.row - 1, location.col - 1)
      case (true, false) =>
        GridLocation(location.row + 1, location.col + 1)
      case (false, false) =>
        GridLocation(location.row - 1, location.col + 1)
    }
    // decide what to do: move/attack/spawnnew

    // return location, what to do (
    newLoc
  }
}
