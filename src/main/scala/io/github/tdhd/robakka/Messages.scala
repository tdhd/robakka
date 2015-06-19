package io.github.tdhd.robakka

case class GridLocation(x: Int, y: Int)

case class AgentState(id: Long, team: Boolean, location: GridLocation)
case class AgentDeath(id: Long)
