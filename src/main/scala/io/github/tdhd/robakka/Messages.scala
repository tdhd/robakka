package io.github.tdhd.robakka

case class GridLocation(x: Int, y: Int)

case class Agent(id: Long, name: String, location: GridLocation, health: Double = 1.0)
case object ReportAgents
case class AgentReport(agents: List[Agent])

case class SpawnAgent(agent: Agent)
case class DespawnAgent(id: Long)

case class PlotWorld(agents: List[Agent])
