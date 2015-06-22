package io.github.tdhd.robakka

import akka.actor.ActorRef

case class GridLocation(row: Int, col: Int)

case object AgentSelfAction
case class AgentDeath(id: Long)

// agent -> agent
case object Attack

// world to self
case object AnnounceWorldState

// agent -> world
case object GetUniqueAgentID
// world -> agent
case class UniqueAgentID(id: Long)

sealed trait GameEntity {
  def position: GridLocation
}
case class AgentEntity(position: GridLocation, agentId: Long, team: Boolean, health: Double, ref: ActorRef) extends GameEntity
case class GrassEntity(position: GridLocation) extends GameEntity
case class WorldState(entities: List[GameEntity])


