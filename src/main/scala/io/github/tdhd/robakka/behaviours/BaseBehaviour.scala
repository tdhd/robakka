package io.github.tdhd.robakka.behaviours

import io.github.tdhd.robakka._

import scala.reflect.{ ClassTag, classTag }

// act returns a set of commands: move, attack, defend
trait BaseBehaviour {
  def act(entity: World.AgentEntity, worldState: World.State): Agent.CommandSet
}

