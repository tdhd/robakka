package io.github.tdhd.robakka.behaviours

import io.github.tdhd.robakka._
import io.github.tdhd.robakka.Agent.MoveCommand

case object FollowEnemyBehaviour extends BaseBehaviour {
  def act(entity: World.AgentEntity, worldState: World.State) = {
    BehaviourHelpers.getEnemies(entity, worldState)

    val enemiesInReach = BehaviourHelpers.getEnemies(entity, worldState)

    val shootings: List[Option[Agent.ActionCommand]] = enemiesInReach.map {
      case World.AgentEntity(_, _, _, _, ref, _) => Option(Agent.Shoot(ref))
    }

    val moveRandomly = enemiesInReach.isEmpty
    val move: Option[Agent.MoveCommand] = if (moveRandomly) {
      (scala.util.Random.nextBoolean, scala.util.Random.nextBoolean) match {
        case (true, true) => Option(Agent.MoveUp)
        case (false, true) => Option(Agent.MoveDown)
        case (true, false) => Option(Agent.MoveLeft)
        case (false, false) => Option(Agent.MoveRight)
      }
    } else {
      val enemyToFollow = enemiesInReach.head
      if (enemyToFollow.position.row == entity.position.row - 1) {
        Option(Agent.MoveUp)
      } else if (enemyToFollow.position.row == entity.position.row + 1) {
        Option(Agent.MoveDown)
      } else if (enemyToFollow.position.col == entity.position.col - 1) {
        Option(Agent.MoveLeft)
      } else if (enemyToFollow.position.col == entity.position.col + 1) {
        Option(Agent.MoveRight)
      } else {
        Option.empty[Agent.MoveCommand]
      }
    }

    if (shootings.isEmpty) {
      Agent.CommandSet(move = move)
    } else {
      Agent.CommandSet(move = move, action = shootings.head)
    }
  }
}

