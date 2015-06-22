package io.github.tdhd.robakka.behaviours

import io.github.tdhd.robakka._

case object RandomBehaviour extends BaseBehaviour {
  def act(entity: AgentEntity, worldState: WorldState) = {

    val commands: List[AgentCommand] = (1 to 4).map {
      i =>
        (scala.util.Random.nextBoolean, scala.util.Random.nextBoolean) match {
          case (true, true) => MoveUp
          case (false, true) => MoveDown
          case (true, false) => MoveLeft
          case (false, false) => MoveRight
        }
    }.toList
    commands
  }
}
