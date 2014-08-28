package com.shoutout.janitorial

import com.shoutout.db.Shoutout
import com.shoutout.util.Dates

import com.shoutout.db.repository._
import org.jets3t.service.impl.rest.httpclient.RestS3Service
import org.jets3t.service.security.AWSCredentials


/**
 * Created by aparrish on 8/28/14.
 */
trait ShoutoutJanitor {

  private lazy val s3 = {
    val awsAccessKey = "AKIAJQEMCJMOSYLFGMXQ"
    val awsSecretKey = "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"
    val awsCredentials = new AWSCredentials(awsAccessKey, awsSecretKey)
    new RestS3Service(awsCredentials)
  }

  val s3ShoutoutBucket = "shoutout-prod-shouts-copy"

  def findAllShoutoutsOlderThan( days : Int ) : List[Shoutout] = {
    val now = Dates.nowDT
    val xDaysAgo = now.plusDays(-1 * days)
    findOlderThan(xDaysAgo.toLocalDate)
  }

  def delete(shoutouts: List[Shoutout]) = {
    deleteAll(shoutouts)
  }

  def deleteS3ObjectsFor( shoutouts : List[Shoutout] ) : Int = {

    val uniqueUrls : Set[String] = shoutouts map { s=> s.imageUrl } toSet

    uniqueUrls foreach { s =>
      val key = s.substring(s.lastIndexOf("/") + 1, s.length)

      try {
        val details = s3.getObjectDetails(s3ShoutoutBucket, key)
        println(s"About to delete: $key")
        s3.deleteObject(s3ShoutoutBucket, details.getKey)
      } catch {
        case e: Exception => println(s"Something went wrong deleting key: $key")
      }
    }

    shoutouts.length
  }

  //TEST ONLY
  //aws s3 sync s3://shoutout-prod-shouts s3://shoutout-prod-shouts-copy --grants full=uri=http://acs.amazonaws.com/groups/global/AllUsers
  //for i in `aws s3 ls s3://shoutout-prod-shouts-copy | tr -s ' ' | cut -d ' ' -f 4`; do aws s3 rm s3://shoutout-prod-shouts-copy/$i; done;
  //then run this
  def updateUrlsForShoutoutTestingAndLocalUse() = {
    testOnlyShoutoutUpdateUrls()
  }

}
