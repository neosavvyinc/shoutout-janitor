package com.shoutout.janitorial

import akka.actor.{Actor, ActorLogging}

/**
  * Created by aparrish on 8/27/14.
 */
object Janitor {

  case object CleanupProfiles
  case object CleanupOldShoutouts
  case object CleanupFullyViewedShoutouts

}

class Janitor extends Actor with ActorLogging {
  import com.shoutout.janitorial.Janitor._

  def receive = {
    case CleanupProfiles =>
      log.info("------------ Begining the Profile Cleanup Process ------------")


      log.info("++++++++++++  Ending the Profile Cleanup Process  ++++++++++++")
    case CleanupOldShoutouts =>
      log.info("--------- Begining the Old Shoutout Cleanup Process ----------")
      log.info("+++++++++  Ending the Old Shoutout Cleanup Process  ++++++++++")

    case CleanupFullyViewedShoutouts =>
      log.info("-------- Begining the Viewed Shoutout Cleanup Process --------")
      log.info("++++++++  Ending the Viewed Shoutout Cleanup Process  ++++++++")
  }


}
