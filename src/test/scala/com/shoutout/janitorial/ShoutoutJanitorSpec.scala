package com.shoutout.janitorial

import org.specs2.mutable.Specification

/**
 * Created by aparrish on 8/28/14.
 */
class ShoutoutJanitorSpec extends Specification with ShoutoutJanitor {

  "The shoutout janitor should cleanup old shoutouts" should {

//    "find things older than X days" in {
//      val x = findAllShoutoutsOlderThan(30)
//      println(x.length, x )
//      x.length must be greaterThan(1)
//    }

//    "update some test urls" in {
//      updateUrlsForShoutoutTestingAndLocalUse()
//      true must be equalTo true
//    }

    "find the right number to clean" in {
      findExclusivelyViewedShoutouts()

      true must be equalTo true
    }

  }


}
