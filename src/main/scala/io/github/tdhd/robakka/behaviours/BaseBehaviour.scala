package io.github.tdhd.robakka.behaviours

import io.github.tdhd.robakka.GridLocation

// TODO: define here what run should return
// act has to return a set of commands: move, attack, defend
trait BaseBehaviour {
  def act(): GridLocation
}

