package io.github.tdhd.robakka

import akka.actor.ActorRef

case class GridLocation(row: Int, col: Int)

case class AgentState(id: Long, location: GridLocation, team: Boolean, health: Double, ref: ActorRef)
case class AgentDeath(id: Long)

// current state of the world
case class WorldState(state: Map[Long, AgentState])

case object Attack
