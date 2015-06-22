package io.github.tdhd.robakka.behaviours

import io.github.tdhd.robakka._

// act returns a set of commands: move, attack, defend
trait BaseBehaviour {
  def act(selfState: AgentState, worldState: Map[Long, AgentState]): List[AgentCommand]
}

