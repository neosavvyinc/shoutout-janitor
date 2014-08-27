package com.shoutout.janitorial

import akka.actor.{ActorRef, Props, ActorSystem, Actor}


/**
 * Created by aparrish on 8/27/14.
 */
object JanitorSystem extends App {

  val system = ActorSystem("JanitorSystem")
  implicit val ec = system.dispatcher

  val janitor = system.actorOf(Props[Janitor])

  system.shutdown()

}
