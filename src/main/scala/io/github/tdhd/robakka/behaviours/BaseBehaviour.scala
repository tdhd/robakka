package io.github.tdhd.robakka.behaviours

import io.github.tdhd.robakka.GridLocation

// TODO: define here what run should return
trait BaseBehaviour {
  def act(): GridLocation
//  def location: GridLocation
//  def worldState: Map[Long, AgentState]
}

