package com.example

object Supervise extends App {
  val s = ActorSystem(SupervisingActor(), "supervise")
  s ! "start"
}

object Main3 {
  def apply(): Behavior[String] = Behaviors.setup(context => new Main3(context))
}

class Main3(context: ActorContext[String])
    extends AbstractBehavior[String](context) {
  override def onMessage(msg: String): Behavior[String] =
    msg match {
      case "start" =>
        val firstRef = context.spawn(SupervisingActor(), "supervising-actor")
        firstRef ! "failChild"
        this
    }
}

object SupervisingActor {
  def apply(): Behavior[String] =
    Behaviors.setup(context => new SupervisingActor(context))
}

class SupervisingActor(context: ActorContext[String])
    extends AbstractBehavior[String](context) {
  private val child =
    context.spawn(
      Behaviors
        .supervise(ChildActor())
        .onFailure(SuperviserStrategy.restart),
      name = "child-actor"
    )

  override def onMessage(msg: String): Behavior[String] =
    msg match {
      case "failChild" =>
        child ! "fail"
        this
    }
}

object Child {
  def apply(): Behavior[String] = Behaviors.setup(context => new Child(context))
}

class Child(context: ActorContext[String]) extends AbstractBehavior[String](context) {
  println("child-actor started")

  override def onMessage(msg: String): Behavior[String] =
    msg match {
      case "fail" =>
        println("child actor fails now")
        throw new Exception("I failed")
    }

  override
}