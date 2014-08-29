package com.shoutout.janitorial

import com.shoutout.db.{ShoutoutCleanupResult, Shoutout}
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

  def delete(shoutouts: List[Shoutout]) = {
    deleteAll(shoutouts)
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

    shoutouts.length
  }

  def findExclusivelyViewedShoutouts() = {
    val shoutouts = findShoutoutsToClean()

    shoutouts.foreach { s : ShoutoutCleanupResult => println(s) }

    val numToClean = shoutouts.length
    println(s"Found this many: $numToClean shoutouts to clean up")
    
    shoutouts
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

  //TEST ONLY
  //aws s3 sync s3://shoutout-prod-shouts s3://shoutout-prod-shouts-copy --grants full=uri=http://acs.amazonaws.com/groups/global/AllUsers
  //for i in `aws s3 ls s3://shoutout-prod-shouts-copy | tr -s ' ' | cut -d ' ' -f 4`; do aws s3 rm s3://shoutout-prod-shouts-copy/$i; done;
  //then run this
  def updateUrlsForShoutoutTestingAndLocalUse() = {
    testOnlyShoutoutUpdateUrls()
  }

}
