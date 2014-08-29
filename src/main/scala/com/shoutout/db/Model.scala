package com.shoutout.db

import java.util.UUID

import org.joda.time.LocalDate

case class ShoutoutUser(id : Option[Long],
                        profilePictureUrl : Option[String])

case class Shoutout(id : Option[Long],
                    imageUrl : String,
                    isViewed : Boolean,
                    viewedByDate : Option[LocalDate],
                    createdTimestamp : LocalDate,
                    isClean : Boolean = false)

case class ShoutoutCleanupResult(id : Option[Long],
                                 imageUrl : String,
                                 isViewed : Boolean)

