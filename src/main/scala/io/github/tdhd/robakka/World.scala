package io.github.tdhd.robakka

import language.postfixOps
import scala.concurrent.duration._
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorLogging
import akka.actor.Props
import akka.actor.Terminated
import akka.actor.Cancellable

object World {
  def props(): Props = Props(new World())
}

class World extends Actor with ActorLogging {
  import context.dispatcher

  var agents = List.empty[Agent]

  override def preStart(): Unit = {
//    context.actorOf(ListeningActor.props(), "listener")
//    // spawn 10 to 20 actors
//    val nActors = 10 + scala.util.Random.nextInt(10)
//    for(i <- 1 to nActors) {
//      val timeToLive = scala.util.Random.nextFloat.seconds
//      context.watch(context.actorOf(DyingActor.props(timeToLive)))
//    }
  }

  def receive = {
    case SpawnAgent(agent) =>
      agents :+= agent
    case DespawnAgent(id) =>
      agents = agents.filterNot(_.id == id)
    case ReportAgents =>
      sender ! AgentReport(agents)

//    case Terminated(ref) =>
//      log.info("{} terminated, {} children left", ref, context.children.size)
//      if (context.children.size.equals(1)){
//        // stop the listening actor
//        context.children.foreach(context.stop)
//        log.info("Dying!")
//        context.stop(self)
//      }
  }
}
