package com.shoutout.janitorial

import akka.actor.{ActorRef, Props, ActorSystem, Actor}

import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension


/**
 * Created by aparrish on 8/27/14.
 */
object JanitorSystem extends App {

  val system = ActorSystem("JanitorSystem")
  implicit val ec = system.dispatcher

  val scheduler = QuartzSchedulerExtension(system)
  val janitor = system.actorOf(Props[Janitor])

  scheduler.schedule("ProfilePictureCleanupService", janitor, Janitor.CleanupProfiles)
  scheduler.schedule("OldShoutoutCleanupService", janitor, Janitor.CleanupOldShoutouts)
  scheduler.schedule("ViewedShoutoutCleanupService", janitor, Janitor.CleanupFullyViewedShoutouts)
  scheduler.schedule("OrphanedShoutoutImages", janitor, Janitor.CleanupOrphanedShoutoutImages)
  scheduler.schedule("SummaryMailer", janitor, Janitor.SendMailSummary)
  scheduler.schedule("SendUnreadMessagesNotification", janitor, Janitor.SendUnreadMessagesNotification)

}

