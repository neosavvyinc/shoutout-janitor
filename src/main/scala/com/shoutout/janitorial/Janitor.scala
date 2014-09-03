package com.shoutout.janitorial

import akka.actor.{Actor, ActorLogging}
import com.shoutout.db.{JanitorFlatStat, JanitorStat, Shoutout}
import com.shoutout.db.repository._
import com.shoutout.util.{Stats, MandrillConfiguration, MandrillUtil, Dates}
import org.jets3t.service.model.S3Object

/**
  * Created by aparrish on 8/27/14.
 */
object Janitor {

  case object CleanupProfiles
  case object CleanupOldShoutouts
  case object CleanupFullyViewedShoutouts
  case object CleanupOrphanedShoutoutImages
  case object SendMailSummary

}

class Janitor extends Actor with ActorLogging with ProfileJanitor with ShoutoutJanitor with JanitorConfig {
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


      updateCleanedProfileStats(profilesCleaned);
      log.info(s"                    Cleaned up $profilesCleaned urls           ")
      log.info("++++++++++++  Ending the Profile Cleanup Process  ++++++++++++")

    case CleanupOldShoutouts =>
      log.info("--------- Begining the Old Shoutout Cleanup Process ----------")

      // find all shoutouts older than X days
      val oldShoutouts = findAllShoutoutsOlderThan(JanitorSettings.age)

      val numDeleted = deleteS3ObjectsFor(oldShoutouts)

      updateAsCleaned(oldShoutouts)

      updateOldShoutoutStats(numDeleted)

      log.info(s"                    Cleaned up $numDeleted urls           ")
      log.info("+++++++++  Ending the Old Shoutout Cleanup Process  ++++++++++")

    case CleanupFullyViewedShoutouts =>
      log.info("-------- Begining the Viewed Shoutout Cleanup Process --------")

      val shoutsToClean = findExclusivelyViewedShoutouts()
      val shoutouts = shoutsToClean.map{ s => Shoutout(s.id, s.imageUrl, s.isViewed, None, Dates.nowLD)}
      val numDeleted = deleteS3ObjectsFor( shoutouts )

      updateAsCleaned(shoutouts)

      updateFullyViewedShoutsStats(numDeleted)

      log.info(s"                    Cleaned up $numDeleted urls           ")
      log.info("++++++++  Ending the Viewed Shoutout Cleanup Process  ++++++++")

    case CleanupOrphanedShoutoutImages =>
      log.info("----- Begining the Shoutout Orphan Image Cleanup Process -----")

      val imageUrls : List[(String, S3Object)] = findAllS3ShoutoutImagesInBucket()
      var shoutoutsCleaned = 0

      imageUrls map { s3tup =>
        if( isOrphanedShoutoutImage(s3tup._1) ) {
          deleteImageFromS3( s3tup._1, s3tup._2 )
          shoutoutsCleaned = shoutoutsCleaned + 1
        }
      }

      updateOrphanedShoutoutImageStats( shoutoutsCleaned )

      log.info(s"                 Cleaned up $shoutoutsCleaned urls                ")
      log.info("+++++  Ending the Shoutout Orphan Image Cleanup Process  +++++")


    case SendMailSummary =>

      log.info("----- Sending out the results!!! -----")

      val currentStats = findFlatStats()

      var stats = new Stats()
      stats.profileCleanup = currentStats.profileCleanup
      stats.oldShoutoutsCleanup = currentStats.oldShoutoutsCleanup
      stats.fullyViewedCleanup = currentStats.fullyViewedCleanup
      stats.orphanedShoutsCleanup = currentStats.orphanedShoutsCleanup

      stats.alltimeProfileCleanup = currentStats.alltimeProfileCleanup
      stats.alltimeOldShoutoutsCleanup = currentStats.alltimeOldShoutoutsCleanup
      stats.alltimeFullyViewedCleanup = currentStats.alltimeFullyViewedCleanup
      stats.alltimeOrphanedShoutsCleanup = currentStats.alltimeOrphanedShoutsCleanup

      val formattedUpdateString = s"""
        | <br/> We cleaned up a lot of stuff today:
        | <br/>
        | <br/> ${stats.profileCleanup} Profile Images were cleaned from S3.
        | <br/> ${stats.oldShoutoutsCleanup} Old Shoutouts were cleaned from S3 and the DB.
        | <br/> ${stats.fullyViewedCleanup} Fully Viewed Shoutouts were cleaned from S3 and the DB.
        | <br/> ${stats.orphanedShoutsCleanup} Orphaned Shoutout Images were cleaned from S3.
        | <br/>
        | <br/> Over all time we have cleaned a lot of junk:
        | <br/>
        | <br/> ${stats.alltimeProfileCleanup + stats.profileCleanup} Profile Images were cleaned from S3.
        | <br/> ${stats.alltimeOldShoutoutsCleanup + stats.oldShoutoutsCleanup} Old Shoutouts were cleaned from S3 and the DB.
        | <br/> ${stats.alltimeFullyViewedCleanup + stats.fullyViewedCleanup} Fully Viewed Shoutouts were cleaned from S3 and the DB.
        | <br/> ${stats.alltimeOrphanedShoutsCleanup + stats.orphanedShoutsCleanup} Orphaned Shoutout Images were cleaned from S3.
        | <br/>
        | <br/> That is all, holler at your boy.
        | <br/>
        | <br/> -Janitor
      """.stripMargin

      MandrillUtil.sendMailViaMandrill(
        new MandrillConfiguration(
          MandrillSettings.apiKey,
          MandrillSettings.smtpPort,
          MandrillSettings.smtpHost,
          MandrillSettings.username),
          MandrillSettings.recipients,
          stats,
          formattedUpdateString
      )

      // reset the counters and update the alltime stats
      updateFlatStats( JanitorFlatStat(
        currentStats.id, 0, 0, 0, 0,
        currentStats.profileCleanup + currentStats.alltimeProfileCleanup,
        currentStats.oldShoutoutsCleanup + currentStats.alltimeOldShoutoutsCleanup,
        currentStats.fullyViewedCleanup + currentStats.alltimeFullyViewedCleanup,
        currentStats.orphanedShoutsCleanup + currentStats.alltimeOrphanedShoutsCleanup
        )
      )

      log.info("+++++  Sending out the results complete +++++")

  }


}
