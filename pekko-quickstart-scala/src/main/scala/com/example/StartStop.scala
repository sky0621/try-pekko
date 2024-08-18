package com.example

import org.apache.pekko
import pekko.actor.typed.ActorSystem
import pekko.actor.typed.Behavior
import pekko.actor.typed.PostStop
import pekko.actor.typed.Signal
import pekko.actor.typed.scaladsl.Behaviors
import pekko.actor.typed.scaladsl.ActorContext
import pekko.actor.typed.scaladsl.AbstractBehavior

object StartStop extends App {
  val s = ActorSystem(Main2(), "startStopSystem")
  s ! "start"
}

object Main2 {
  def apply(): Behavior[String] = Behaviors.setup(context => new Main2(context))
}

class Main2(context: ActorContext[String])
    extends AbstractBehavior[String](context) {
  override def onMessage(msg: String): Behavior[String] =
    msg match {
      case "start" =>
        val firstRef = context.spawn(StartStopActor1(), "first")
        firstRef ! "stop"
        this
    }
}

object StartStopActor1 {
  def apply(): Behavior[String] =
    Behaviors.setup(context => new StartStopActor1(context))
}

class StartStopActor1(context: ActorContext[String])
    extends AbstractBehavior[String](context) {
  println("first started")
  context.spawn(StartStopActor2(), "second")

  override def onMessage(msg: String): Behavior[String] =
    msg match {
      case "stop" => Behaviors.stopped
    }

  override def onSignal: PartialFunction[Signal, Behavior[String]] = {
    case PostStop =>
      println("first stopped")
      this
  }
}

object StartStopActor2 {
  def apply(): Behavior[String] =
    Behaviors.setup(new StartStopActor2(_))
}

class StartStopActor2(context: ActorContext[String])
    extends AbstractBehavior[String](context) {
  println("second started")

  override def onMessage(msg: String): Behavior[String] = Behaviors.unhandled

  override def onSignal: PartialFunction[Signal, Behavior[String]] = {
    case PostStop =>
      println("second stopped")
      this
  }
}
