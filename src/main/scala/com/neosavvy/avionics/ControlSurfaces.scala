package com.neosavvy.avionics

import akka.actor.{ActorRef, Actor}

/**
 * Created by aparrish on 8/27/14.
 */
object ControlSurfaces {

  case class StickBack(amount : Float)
  case class StickForward(amount : Float)

}

class ControlSurfaces(altimeter:ActorRef) extends Actor {

  import ControlSurfaces._
  import Altimeter._

  def receive = {
    case StickBack(amount) =>
      altimeter ! RateChange(amount)
    case StickForward(amount) =>
      altimeter ! RateChange(-1 * amount)
  }

}
