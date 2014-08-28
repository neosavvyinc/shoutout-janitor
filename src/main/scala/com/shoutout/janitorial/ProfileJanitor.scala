package com.shoutout.janitorial

import com.shoutout.db.ShoutoutUser
import org.jets3t.service.impl.rest.httpclient.RestS3Service
import org.jets3t.service.model.S3Object
import org.jets3t.service.security.AWSCredentials
import com.shoutout.db.repository._

/**
 * Created by aparrish on 8/27/14.
 */
trait ProfileJanitor {

  private lazy val s3 = {
    val awsAccessKey = "AKIAJQEMCJMOSYLFGMXQ"
    val awsSecretKey = "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"
    val awsCredentials = new AWSCredentials(awsAccessKey, awsSecretKey)
    new RestS3Service(awsCredentials)
  }

  val s3Bucket = "shoutout-prod-profile-copy"

  def findAllS3ImagesInBucket() : List[(String, S3Object)] = {

      val objects = s3.listObjects(s3Bucket).toList

      objects map { obj =>
        s3.getObjectDetails(s3Bucket, obj.getKey)

        (s3.createUnsignedObjectUrl(s3Bucket,
          obj.getKey,
          false, true, false), obj)

      }

  }

  def isOrphanedProfileImage( url : String ) : Boolean = {
    val users = findUsersByUrl(url)
    users.length == 0
  }

  def deleteImageFromS3( url : String, s3Object: S3Object ) = {
    s3.deleteObject(s3Bucket, s3Object.getKey)
  }

  ///TEST ONLY
  def updateUrlsForTestingAndLocalUse() = {
    testOnlyUpdateUrls()
  }

}

