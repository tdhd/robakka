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

  def plot(agents: List[Agent]) = {
    val result = new org.jfree.data.xy.XYSeriesCollection()
    val series = new org.jfree.data.xy.XYSeries("Random")

    agents.foreach{
      case Agent(id, name, GridLocation(x, y), _) =>
        series.add(x, y)
    }

    result.addSeries(series)
    val chart = org.jfree.chart.ChartFactory.createScatterPlot(
      "Scatter Plot", // chart title
      "X", // x axis label
      "Y", // y axis label
      result)
//      PlotOrientation.VERTICAL,
//      true, // include legend
//      true, // tooltips
//      false // urls

    // create and display a frame...
    val frame = new org.jfree.chart.ChartFrame("First", chart)
    frame.pack()
    frame.setVisible(true)
  }

  def receive = {
    case PlotWorld(agents) =>
      plot(agents)
  }
}
