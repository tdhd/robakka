# robakka

robakka is a programming game in scala. It is based on akka (http://akka.io) to model the agents in the game. Also it is not round based but implemented to allow the agents to react in real-time to events. Agents are customized by implementing a certain behaviour.

This game is inspired by RobotWar (https://en.wikipedia.org/wiki/RobotWar) and cells (https://github.com/phreeza/cells).

### Behaviours in robakka

The main challenge of the game is to implement behaviours which can beat others. I provide some pretty simple behaviours and will continue to add more as development progresses.

For example check out `FollowPlantBehaviour` which will cause all agents with the installed behaviour to consume a plant if one is in their neighbourhood. If there is no plant, the behaviour causes the agent to randomly search for another plant. All agents with this behaviour are pacifists, they only eat plants and never shoot anyone! The implementation of it is quite simple:

```{scala}
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

```

### About robakka

robakka is in early development and barely useable at the moment.

### Why the name robakka?

robakka is a combination of robot and akka, the underlying modelling framework of the agents.

