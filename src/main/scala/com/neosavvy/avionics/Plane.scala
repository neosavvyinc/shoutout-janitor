package com.neosavvy.avionics

import akka.actor.{Props, Actor, ActorLogging}
import com.neosavvy.avionics.EventSource.RegisterListener

/**
 * Created by aparrish on 8/27/14.
 */
object Plane {
  case object GiveMeControl
}

class Plane extends Actor with ActorLogging {

  import Altimeter._
  import Plane._

  val altimeter = context.actorOf( Props[Altimeter], "Altimeter" )
  val controls = context.actorOf( Props(new ControlSurfaces(altimeter)), "ControlSurfaces" )

  override def preStart = {
    altimeter ! RegisterListener(self)
  }

  def receive = {
    case AltitudeUpdate(altitude) =>
      log.info(s"Altitude is now $altitude")
    case GiveMeControl =>
      log.info("Plane giving control.")
      sender ! controls
  }

}
