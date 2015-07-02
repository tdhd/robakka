package io.github.tdhd.robakka.behaviours

import io.github.tdhd.robakka._
import io.github.tdhd.robakka.Agent.MoveCommand

case object FollowPlantBehaviour extends BaseBehaviour {
  def act(entity: World.AgentEntity, worldState: World.State) = {

    val plants = BehaviourHelpers.entities2MoveCommand[World.PlantEntity](entity, worldState)

    val move: Option[Agent.MoveCommand] = if (plants.isEmpty) {
      Option(BehaviourHelpers.getRandomMove)
    } else {
      Option(plants.head._2)
    }

    Agent.CommandSet(move = move)
  }
}
