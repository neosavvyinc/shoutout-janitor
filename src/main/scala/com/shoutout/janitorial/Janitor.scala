package com.shoutout.janitorial

import akka.actor.{Actor, ActorLogging}

/**
 * Created by aparrish on 8/27/14.
 */
object Janitor {

}

class Janitor extends Actor with ActorLogging {

  def receive = {
    case _ => {
      log.debug("I'm receiving some goods...")
    }
  }


}
