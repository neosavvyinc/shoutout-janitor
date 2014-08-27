package com.neosavvy.investigation

import akka.actor.{Props, ActorSystem, Actor}


/**
 * Created by aparrish on 8/26/14.
 */
class BadShakespeareanActor extends Actor {
  def receive: Receive = {
    case "Good Morning" => println("Him: Forsooth 'tis the 'morn, but morneth for thou doest I do!");
    case "You're terrible" => println("Him: Yup")
  }
}

object BadShakespeareanActorMain {
  val system = ActorSystem("BadShakespearean")
  val actor = system.actorOf(Props[BadShakespeareanActor])

  def send ( msg : String ): Unit = {
    println(s"Me: $msg")
    actor ! msg
    Thread.sleep(100)
  }

  def main(args : Array[String]): Unit = {
    send("Good Morning")
    send("You're terrible")
    system.shutdown()
  }
}
