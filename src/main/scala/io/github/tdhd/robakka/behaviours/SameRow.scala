package io.github.tdhd.robakka.behaviours

import io.github.tdhd.robakka._

case object SameRowBehaviour extends BaseBehaviour {
  def act(entity: World.AgentEntity, worldState: World.State) = {
    val enemiesOnSameRow = worldState.entities.filter {
      case World.AgentEntity(World.Location(row, col), id, team, health, ref, world) =>
        id != entity.agentId && team != entity.team && row == entity.position.row
      case _ => false
    }

    val shootings: List[Agent.ActionCommand] = enemiesOnSameRow.map {
      case World.AgentEntity(_, _, _, _, ref, _) => Agent.Shoot(ref)
    }

    // movement depends on whether on the same row there is a teammate
    val teamOnSameRow = !worldState.entities.filter {
      case World.AgentEntity(World.Location(row, col), id, team, health, ref, world) =>
        id != entity.agentId && team == entity.team && row == entity.position.row
      case _ => false
    }.isEmpty

    val move: Agent.MoveCommand = if (teamOnSameRow) {
      if (scala.util.Random.nextBoolean) {
        Agent.MoveLeft
      } else {
        Agent.MoveRight
      }
    } else {
      if (scala.util.Random.nextBoolean) {
        Agent.MoveDown
      } else {
        Agent.MoveUp
      }
    }

    if (shootings.isEmpty) {
      Agent.CommandSet(move = Option(move))
    } else {
      Agent.CommandSet(move = Option(move), action = Option(shootings.head))
    }
  }
}

