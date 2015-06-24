package io.github.tdhd.robakka

import akka.actor.ActorRef


sealed trait AgentCommand

sealed trait MoveCommand extends AgentCommand
case object MoveUp extends MoveCommand
case object MoveDown extends MoveCommand
case object MoveLeft extends MoveCommand
case object MoveRight extends MoveCommand

sealed trait ActionCommand extends AgentCommand
case class Shoot(ref: ActorRef) extends ActionCommand

// defines the set of command an agent can issue at one given point in time
case class CommandSet(move: Option[MoveCommand] = Option.empty[MoveCommand], action: Option[ActionCommand] = Option.empty[ActionCommand])
