package io.github.tdhd.robakka

import language.postfixOps
import scala.concurrent.duration._
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorLogging
import akka.actor.Props
import akka.actor.Terminated
import akka.actor.Cancellable

object Game {
  def props(): Props = Props(new Game())
}

class Game extends Actor with ActorLogging {
  import context.dispatcher

  // // event stream subscriptions
  //context.system.eventStream.subscribe(self, classOf[DyingCry])
//  override def postStop(): Unit = {
//    log.info("Dying!")
//    context.system.eventStream.unsubscribe(self)
//  }

  // scheduler
  //val scheduler: Cancellable = context.system.scheduler.scheduleOnce(timeToLive)(die)
  //def die() = {
  //  context.system.eventStream.publish(DyingCry(s"${context.parent} let me die!"))
  //  scheduler.cancel
  //  context.stop(self)
  //}

  val world = context.watch(context.actorOf(World.props(), "world"))
  val visualizer = context.watch(context.actorOf(Visualizer.props(), "visualizer"))

//  // can select on filtering
//  val w = context.children.filter{_.path.name.compareTo("world") == 0}.head
//  println(w)
//  context.children.foreach{
//    child =>
//      println(child.path.name)
//  }

  context.children.foreach{println}

  // deprecated
//  println("selecting visualizer actor")
//  val a = context.actorSelection("visualizer")
//  println(a)

  override def preStart() = {
    // TODO: agents should be actors, too!
    world ! SpawnAgent(Agent(1, "colombo", GridLocation(100, 100)))
    world ! SpawnAgent(Agent(2, "sherlock", GridLocation(110, 100)))
    world ! ReportAgents
    world ! DespawnAgent(1)
    world ! ReportAgents
  }

  def receive = {
    case AgentReport(agents) =>
      println(s"Received ${agents.size} agents back")
      agents.foreach{println}
      println("---")
      visualizer ! PlotWorld(agents)

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
