package io.github.tdhd.robakka

import akka.actor.ActorRef

case class GridLocation(row: Int, col: Int)

case object AgentSelfAction
case class AgentState(
    id: Long,
    location: GridLocation,
    team: Boolean,
    health: Double,
    ref: ActorRef)
case class AgentDeath(id: Long)

// current state of the world
// map from agent id to state of agent
case class WorldState(state: Map[Long, AgentState])

case object Attack
