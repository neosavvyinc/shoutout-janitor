package com.shoutout.janitorial

import org.specs2.mutable.Specification

/**
 * Created by aparrish on 8/27/14.
 */
class ProfileJanitorSpec extends Specification with ProfileJanitor {

  "The s3 listing should produce a list of strings" should {

    "listing" in {

      val listing = findAllS3ImagesInBucket()

      listing.length must be greaterThan 0

    }

    "isOrphaned" in {

      val isNotOrphaned = isOrphanedProfileImage("https://shoutout-prod-profile-copy.s3.amazonaws.com/23201AFAD970623F9DD131F9D9BC34CC")
      val isOrphaned = isOrphanedProfileImage("somethingNonexistent")

      (isNotOrphaned must be equalTo false ) and (isOrphaned must be equalTo true)
    }
//
//    "test update stuff" in {
//      updateUrlsForTestingAndLocalUse();
//
//      true must be equalTo true
//    }

    "delete image from S3" in {



    }
  }

}
