package com.shoutout.janitorial

import com.shoutout.db.{JanitorStat, ShoutoutUser}
import com.shoutout.util.Dates
import org.jets3t.service.impl.rest.httpclient.RestS3Service
import org.jets3t.service.model.S3Object
import org.jets3t.service.security.AWSCredentials
import com.shoutout.db.repository._

/**
 * Created by aparrish on 8/27/14.
 */
trait ProfileJanitor extends JanitorConfig {

  private lazy val s3 = {
    val awsCredentials = new AWSCredentials(
      S3Configuration.accessKey,
      S3Configuration.secretKey)
    new RestS3Service(awsCredentials)
  }

  def findAllS3ImagesInBucket() : List[(String, S3Object)] = {

      val objects = s3.listObjects(S3Configuration.profileBucket).toList

      objects map { obj =>
        s3.getObjectDetails(S3Configuration.profileBucket, obj.getKey)

        (s3.createUnsignedObjectUrl(S3Configuration.profileBucket,
          obj.getKey,
          false, true, false), obj)

      }

  }

  def isOrphanedProfileImage( url : String ) : Boolean = {
    val users = findUsersByUrl(url)
    users.length == 0
  }

  def deleteImageFromS3( url : String, s3Object: S3Object ) = {
    s3.deleteObject(S3Configuration.profileBucket, s3Object.getKey)
  }

  def updateCleanedProfileStats( count : Int ) = {
    val currentStats = findFlatStats()
    updateFlatStats(currentStats.copy(
      profileCleanup = count + currentStats.profileCleanup
    ))
  }

  ///TEST ONLY
  def updateUrlsForTestingAndLocalUse() = {
    testOnlyUpdateUrls()
  }

}

