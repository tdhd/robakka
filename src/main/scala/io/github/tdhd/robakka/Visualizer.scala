package io.github.tdhd.robakka

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

// http://stackoverflow.com/questions/19065053/animate-plot-on-jfreechart-line-graph

object Visualizer {
  def props(world: ActorRef, worldSize: Size): Props = Props(new Visualizer(world, worldSize))
}

class Visualizer(world: ActorRef, worldSize: Size) extends Actor with ActorLogging {
  import context.dispatcher
  //  implicit val timeout = Timeout(200 milliseconds)

  // subscribe to events of the world
  context.system.eventStream.subscribe(self, classOf[WorldState])

  override def postStop() = {
    context.system.eventStream.unsubscribe(self)
  }

  //  def plot(agents: List[AgentStatus]) = {
  //    val result = new org.jfree.data.xy.XYSeriesCollection()
  //    val series = new org.jfree.data.xy.XYSeries("Random")
  //
  //    agents.foreach{
  //      case AgentStatus(name, GridLocation(x, y)) =>
  //        series.add(x, y)
  //    }
  //
  //    result.addSeries(series)
  //    val chart = org.jfree.chart.ChartFactory.createScatterPlot(
  //      "Scatter Plot", // chart title
  //      "X", // x axis label
  //      "Y", // y axis label
  //      result)
  ////      PlotOrientation.VERTICAL,
  ////      true, // include legend
  ////      true, // tooltips
  ////      false // urls
  //
  //    // create and display a frame...
  //    val frame = new org.jfree.chart.ChartFrame("First", chart)
  //    frame.pack()
  //    frame.setVisible(true)
  //  }

  def plotVisualState(visualState: WorldState) = {
    // TODO: no more than three teams
    val teamVisualizers = List("x", "y", "z")
    val uniqueTeams = visualState.entities.filter {
      case agent: AgentEntity => true
      case _ => false
    }.map { _.asInstanceOf[AgentEntity].team }.distinct.zipWithIndex
    assert(uniqueTeams.size <= 3)

    // print team status
    uniqueTeams.foreach {
      case (teamId, index) =>
        val n = visualState.entities.filter {
          case AgentEntity(GridLocation(row, col), id, team, health, ref, world) => team == teamId
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
        case AgentEntity(GridLocation(row, col), id, team, health, ref, world) => row == i && col == j
        case _ => false
      }

      if (agents.isEmpty) {
        print(" ")
      } else {
        if (agents.size > 1) {
          print(agents.size)
        } else {
          agents.foreach {
            case AgentEntity(GridLocation(row, col), id, team, health, ref, world) =>
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
    case ws: WorldState => plotVisualState(ws)
  }
}
