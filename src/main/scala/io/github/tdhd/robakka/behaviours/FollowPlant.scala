package io.github.tdhd.robakka.behaviours

import io.github.tdhd.robakka._
import io.github.tdhd.robakka.Agent.MoveCommand

case object FollowPlantBehaviour extends BaseBehaviour {
  def act(entity: World.AgentEntity, worldState: World.State) = {

    val plants = BehaviourHelpers.entities2MoveCommand[World.PlantEntity](entity, worldState)

    val move = plants.headOption.map {
      case (plant, moveCommand) => moveCommand
    }.getOrElse(BehaviourHelpers.getRandomMove)

    Agent.CommandSet(move = Option(move))
  }
}
