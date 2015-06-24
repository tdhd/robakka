package io.github.tdhd.robakka.behaviours

import io.github.tdhd.robakka._

case object RandomBehaviour extends BaseBehaviour {
  def act(entity: AgentEntity, worldState: WorldState) = {
    val move = (scala.util.Random.nextBoolean, scala.util.Random.nextBoolean) match {
      case (true, true) => MoveUp
      case (false, true) => MoveDown
      case (true, false) => MoveLeft
      case (false, false) => MoveRight
    }
    CommandSet(move = Option(move))
  }
}
