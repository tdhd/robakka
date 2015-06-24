package io.github.tdhd.robakka

import language.postfixOps
import scala.concurrent.duration._
import scala.collection.mutable
import scala.concurrent.Future
import scala.util.{ Failure, Success, Try }
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.ActorLogging
import akka.actor.Cancellable
import akka.actor.Props

case class RobakkaConfig(agentBehaviours: Seq[String] = List(""))

object Robakka {
  def main(args: Array[String]): Unit = {

    val parser = new scopt.OptionParser[RobakkaConfig]("Robakka") {
      head("Robakka", "0.1")
      opt[Seq[String]]('b', "behaviours") required () valueName ("SameRow,Random,...") action { (x, c) =>
        c.copy(agentBehaviours = x)
      } text ("agent behaviours to install")
      help("help") text ("print help")
    }

    // if cmdLineConfig is not Some(...) then
    // the arguments are bad, error message will have been displayed
    parser.parse(args, RobakkaConfig()).foreach {
      cmdLineConfig =>
        val teams = cmdLineConfig.agentBehaviours.map(_.toLowerCase).map {
          case "samerow" => io.github.tdhd.robakka.behaviours.SameRowBehaviour
          case "random" => io.github.tdhd.robakka.behaviours.RandomBehaviour
          case "leftmoveshootright" => io.github.tdhd.robakka.behaviours.LeftMoveShootRightBehaviour
        }.zipWithIndex.map(x => GameTeam(x._2, x._1))

        val system = ActorSystem("system")
        val game = system.actorOf(Game.props(teams), "Game")

        Thread.sleep(30000)
        //    system.awaitTermination(5 seconds)
        system.shutdown()
    }
  }
}
