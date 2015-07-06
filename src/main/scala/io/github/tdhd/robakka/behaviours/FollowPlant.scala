package io.github.tdhd.robakka.behaviours

import io.github.tdhd.robakka._
import io.github.tdhd.robakka.Agent.MoveCommand

case object FollowPlantBehaviour extends BaseBehaviour {
  def act(entity: World.AgentEntity, worldState: World.StateContainer) = {

    val plants = BehaviourHelpers.entities2MoveCommand(entity, worldState)

    val move = plants.collect {
      case (plant: World.PlantEntity, moveCommand) => moveCommand
    }.headOption.getOrElse(BehaviourHelpers.getRandomMove)

    Agent.CommandSet(move = Option(move))
  }
}
