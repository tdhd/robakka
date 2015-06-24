package io.github.tdhd.robakka.visualization

// TODO: https://github.com/wookietreiber/scala-chart ?

object VisualizerTest {

  def plotAnimated() = {
    val result = new org.jfree.data.xy.XYSeriesCollection()
    val series = new org.jfree.data.xy.XYSeries("Random")

    (1 to 20).foreach {
      n => series.add(scala.util.Random.nextDouble, scala.util.Random.nextDouble)
    }
    result.addSeries(series)
    val chart = org.jfree.chart.ChartFactory.createScatterPlot(
      "Scatter Plot", // chart title
      "X", // x axis label
      "Y", // y axis label
      result)

    // create and display a frame...
    val frame = new org.jfree.chart.ChartFrame("First", chart)
    frame.pack()
    frame.setVisible(true)
  }

  def plot() = {
    val result = new org.jfree.data.xy.XYSeriesCollection()
    val series = new org.jfree.data.xy.XYSeries("Random")

    (1 to 20).foreach {
      n => series.add(scala.util.Random.nextDouble, scala.util.Random.nextDouble)
    }
    //    agents.foreach {
    //      case AgentStatus(name, GridLocation(x, y)) =>
    //        series.add(x, y)
    //    }

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

  def main(args: Array[String]) = {
    println("yay")
    for (i <- 1 to 100) {
      plot()
      Thread.sleep(1000)
    }
  }
}
