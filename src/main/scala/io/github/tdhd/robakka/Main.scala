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

object Robakka {
  def main(args: Array[String]): Unit = {

    val system = ActorSystem("system")
    val game = system.actorOf(Props[Game], "Game")

    Thread.sleep(15000)
//    system.awaitTermination(5 seconds)
    system.shutdown()
  }
}
