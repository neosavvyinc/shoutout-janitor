package com.shoutout.db

import java.util.UUID

import org.joda.time.{DateTime, LocalDate}

case class ShoutoutUser(id : Option[Long],
                        profilePictureUrl : Option[String])

case class Shoutout(id : Option[Long],
                    imageUrl : String,
                    isViewed : Boolean,
                    viewedByDate : Option[LocalDate],
                    createdTimestamp : LocalDate,
                    isClean : Boolean = false,
                    isBlocked : Boolean = false)

case class ShoutoutCleanupResult(id : Option[Long],
                                 imageUrl : String,
                                 isViewed : Boolean)


case class JanitorStat(id : Option[Long],
                       actionPerformed : String,
                       actionDate : LocalDate,
                       actionCount : Int)

case class JanitorFlatStat( id : Option[Long]
                            ,profileCleanup : Int
                            ,oldShoutoutsCleanup : Int
                            ,fullyViewedCleanup : Int
                            ,orphanedShoutsCleanup : Int
                            ,alltimeProfileCleanup : Int
                            ,alltimeOldShoutoutsCleanup : Int
                            ,alltimeFullyViewedCleanup : Int
                            ,alltimeOrphanedShoutsCleanup : Int
                            )