package io.github.tdhd.robakka.visualization

import language.postfixOps
import scala.concurrent.duration._
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorLogging
import akka.actor.Props
import akka.actor.Terminated
import akka.actor.Cancellable
import akka.util.Timeout
import akka.pattern.{ ask, pipe }

import io.github.tdhd.robakka._

// http://stackoverflow.com/questions/19065053/animate-plot-on-jfreechart-line-graph

object Visualizer {
  def props(world: ActorRef, worldSize: World.Size): Props = Props(new Visualizer(world, worldSize))
}

class Visualizer(world: ActorRef, worldSize: World.Size) extends Actor with ActorLogging {
  import context.dispatcher

  // subscribe to events of the world
  context.system.eventStream.subscribe(self, classOf[World.State])

  override def postStop() = context.system.eventStream.unsubscribe(self)

  def plotVisualState(visualState: World.State) = {
    // TODO: no more than three teams
    val teamVisualizers = List("x", "y", "z")
    val uniqueTeams = visualState.entities.filter {
      case agent: World.AgentEntity => true
      case _ => false
    }.map { _.asInstanceOf[World.AgentEntity].team }.distinct.zipWithIndex
    assert(uniqueTeams.size <= 3)

    // print team status
    uniqueTeams.foreach {
      case (teamId, index) =>
        val n = visualState.entities.filter {
          case World.AgentEntity(World.Location(row, col), id, team, health, ref, world) => team == teamId
          case _ => false
        }.size
        println(s"${teamVisualizers(index)}: $n")
    }

    println("-" * worldSize.nCols)
    for {
      i <- 1 to worldSize.nRows
      j <- 1 to worldSize.nCols
    } {

      val agents = visualState.entities.filter {
        case World.AgentEntity(World.Location(row, col), id, team, health, ref, world) => row == i && col == j
        case _ => false
      }

      if (agents.isEmpty) {
        print(" ")
      } else {
        if (agents.size > 1) {
          print(agents.size)
        } else {
          agents.foreach {
            case World.AgentEntity(World.Location(row, col), id, team, health, ref, world) =>
              val teamColor = teamVisualizers {
                uniqueTeams.filter {
                  case (teamId, index) => teamId == team
                }.map(_._2).head
              }
              print(teamColor)
            case _ => false
          }
        }
      }
      if (j == worldSize.nCols) {
        println("")
      }
    }
    println("-" * worldSize.nCols)
  }

  def receive = {
    case ws: World.State => plotVisualState(ws)
  }
}
