package io.github.tdhd.robakka

import language.postfixOps
import scala.concurrent.duration._
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorLogging
import akka.actor.Props
import akka.actor.Terminated
import akka.actor.Cancellable

// http://stackoverflow.com/questions/19065053/animate-plot-on-jfreechart-line-graph

object Visualizer {
  def props(): Props = Props(new Visualizer())
}

class Visualizer extends Actor with ActorLogging {
  import context.dispatcher

  // subscribe visualizer to events of agents
  // TODO: group these?
  context.system.eventStream.subscribe(self, classOf[AgentState])
  context.system.eventStream.subscribe(self, classOf[AgentDeath])

  override def postStop() = {
    context.system.eventStream.unsubscribe(self)
  }

  var visualState = Map.empty[Long, AgentState]

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

  def plotVisualState() = {
    val nRows = 10
    val nCols = 20
    println("-" * nCols)
    for {
      i <- 1 to nRows
      j <- 1 to nCols
    } {

      val agents = visualState.filter {
        case (id, AgentState(_, team, GridLocation(x, y))) => x == j && y == i
      }
      if (agents.isEmpty) {
        print(" ")
      } else {
        if (agents.size > 1) {
          print(agents.size)
        } else {
          agents.foreach {
            case (id, AgentState(_, team, _)) =>
              if (team) {
                print("x")
              } else {
                print("y")
              }
          }
        }
      }
      if (j == nCols) {
        println("")
      }
    }
    println("-" * nCols)
  }

  def receive = {
    case AgentDeath(id) =>
      // remove agent
      visualState -= id

    case AgentState(id, team, location) =>
      println(s"VISUALIZER received Agentstatus: $id, $location")
      //      if (visualState.contains(id)) {
      visualState -= id
      //      }
      visualState += (id -> AgentState(id, team, location))
      plotVisualState()
  }
}
