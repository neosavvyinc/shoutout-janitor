package com.shoutout.janitorial

import akka.actor.{Actor, ActorLogging}
import org.jets3t.service.model.S3Object

/**
  * Created by aparrish on 8/27/14.
 */
object Janitor {

  case object CleanupProfiles
  case object CleanupOldShoutouts
  case object CleanupFullyViewedShoutouts

}

class Janitor extends Actor with ActorLogging with ProfileJanitor {
  import com.shoutout.janitorial.Janitor._

  def receive = {
    case CleanupProfiles =>
      log.info("------------ Begining the Profile Cleanup Process ------------")

      val imageUrls : List[(String, S3Object)] = findAllS3ImagesInBucket()
      var profilesCleaned = 0

      imageUrls map { s3tup =>
        if( isOrphanedProfileImage(s3tup._1) ) {
          deleteImageFromS3( s3tup._1, s3tup._2 )
          profilesCleaned = profilesCleaned + 1
        }
      }

      log.info(s"                    Cleaned up $profilesCleaned urls           ")
      log.info("++++++++++++  Ending the Profile Cleanup Process  ++++++++++++")

    case CleanupOldShoutouts =>
      log.info("--------- Begining the Old Shoutout Cleanup Process ----------")

      log.info("+++++++++  Ending the Old Shoutout Cleanup Process  ++++++++++")

    case CleanupFullyViewedShoutouts =>
      log.info("-------- Begining the Viewed Shoutout Cleanup Process --------")

      log.info("++++++++  Ending the Viewed Shoutout Cleanup Process  ++++++++")
  }


}
