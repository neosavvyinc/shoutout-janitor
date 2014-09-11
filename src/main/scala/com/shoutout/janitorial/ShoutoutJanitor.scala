package com.shoutout.janitorial

import com.shoutout.db.{JanitorFlatStat, JanitorStat, ShoutoutCleanupResult, Shoutout}
import com.shoutout.util.Dates

import com.shoutout.db.repository._
import org.jets3t.service.impl.rest.httpclient.RestS3Service
import org.jets3t.service.model.S3Object
import org.jets3t.service.security.AWSCredentials


/**
 * Created by aparrish on 8/28/14.
 */
trait ShoutoutJanitor extends JanitorConfig {

  private lazy val s3 = {
    val awsCredentials = new AWSCredentials(S3Configuration.accessKey, S3Configuration.secretKey)
    new RestS3Service(awsCredentials)
  }

  def findAllShoutoutsOlderThan( days : Int ) : List[Shoutout] = {
    val now = Dates.nowDT
    val xDaysAgo = now.plusDays(-1 * days)
    findOlderThan(xDaysAgo.toLocalDate)
  }

  def updateAsCleaned(shoutouts: List[Shoutout]) = {
    updateAllAsCleaned(shoutouts)
  }

  def deleteS3ObjectsFor( shoutouts : List[Shoutout] ) : Int = {
    import scala.language.postfixOps

    val uniqueUrls : Set[String] = shoutouts map { s=> s.imageUrl } toSet

    uniqueUrls foreach { s =>
      val key = s.substring(s.lastIndexOf("/") + 1, s.length)

      try {
        s3.deleteObject(S3Configuration.shoutoutBucket, key)
      } catch {
        case e: Exception => println(s"Something went wrong deleting key: $key")
      }
    }

    uniqueUrls.toList.length
  }

  def deleteShoutoutImageFromS3( url : String, s3Object: S3Object ) = {
    s3.deleteObject(S3Configuration.shoutoutBucket, s3Object.getKey)
  }

  def findExclusivelyViewedShoutouts() = {
    val shoutouts = findShoutoutsToClean()

    val numToClean = shoutouts.length
    println(s"Found this many: $numToClean shoutouts to clean up")
    
    shoutouts
  }

  def findWelcomeShoutouts() = {
    findViewedWelcomeImages()
  }

  def isOrphanedShoutoutImage( url : String ) : Boolean = {
    val shoutouts = findShoutoutsByUrl(url)
    shoutouts.length == 0
  }

  def findAllS3ShoutoutImagesInBucket() : List[(String, S3Object)] = {

    val objects = s3.listObjects(S3Configuration.shoutoutBucket).toList

    objects map { obj =>
      s3.getObjectDetails(S3Configuration.shoutoutBucket, obj.getKey)

      (s3.createUnsignedObjectUrl(S3Configuration.shoutoutBucket,
        obj.getKey,
        false, true, false), obj)

    }

  }

  def updateOldShoutoutStats( shoutoutsCleaned : Int )= {
    val currentStats = findFlatStats()
    updateFlatStats(currentStats.copy(
      oldShoutoutsCleanup = shoutoutsCleaned + currentStats.oldShoutoutsCleanup
    ))
  }
  def updateFullyViewedShoutsStats( shoutoutsCleaned : Int )= {
    val currentStats = findFlatStats()
    updateFlatStats(currentStats.copy(
      fullyViewedCleanup = shoutoutsCleaned + currentStats.fullyViewedCleanup
    ))
  }
  def updateOrphanedShoutoutImageStats( shoutoutsCleaned : Int )= {
    val currentStats = findFlatStats()
    updateFlatStats(currentStats.copy(
      orphanedShoutsCleanup = shoutoutsCleaned + currentStats.orphanedShoutsCleanup
    ))
  }
  def updateS3Stats( shoutoutsCleaned : Int )= {
    val currentStats = findFlatStats()
    updateFlatStats(currentStats.copy(
      s3ImagesCleanup = shoutoutsCleaned + currentStats.s3ImagesCleanup
    ))
  }

  def findCurrentStats() : JanitorFlatStat = {
    findFlatStats()
  }



  //TEST ONLY
  //aws s3 sync s3://shoutout-prod-shouts s3://shoutout-prod-shouts-copy --grants full=uri=http://acs.amazonaws.com/groups/global/AllUsers
  //for i in `aws s3 ls s3://shoutout-prod-shouts-copy | tr -s ' ' | cut -d ' ' -f 4`; do aws s3 rm s3://shoutout-prod-shouts-copy/$i; done;
  //then run this
  def updateUrlsForShoutoutTestingAndLocalUse() = {
    testOnlyShoutoutUpdateUrls()
  }

}
