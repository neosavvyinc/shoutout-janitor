package com.shoutout.db

import java.util.UUID

import com.shoutout.janitorial.JanitorConfig
import org.joda.time.{LocalDate, DateTime}

import scala.slick.ast.ColumnOption.DBType
import scala.slick.direct.AnnotationMapper.column
import scala.slick.lifted.ProvenShape


/**
 * Created by aparrish on 8/27/14.
 */
package object repository extends JanitorConfig {

  import scala.slick.driver.MySQLDriver.simple._
  import com.github.tototoshi.slick.MySQLJodaSupport._
  import scala.slick.jdbc.{GetResult, StaticQuery => Q}

  val db = Database.forURL( DBConfiguration.url,
    driver = DBConfiguration.driver,
    user = DBConfiguration.user,
    password = DBConfiguration.pass)

  /**
   * USERS
   *
   * +------------+---------------+------+-----+---------+----------------+
   * | Field      | Type          | Null | Key | Default | Extra          |
   * +------------+---------------+------+-----+---------+----------------+
   * | ID         | int(11)       | NO   | PRI | NULL    | auto_increment |
   * | FIRST_NAME | varchar(1024) | NO   |     | NULL    |                |
   * | LAST_NAME  | varchar(1024) | NO   |     | NULL    |                |
   * | EMAIL      | varchar(1024) | NO   |     | NULL    |                |
   * +------------+---------------+------+-----+---------+----------------+
   */
  class ShoutoutUserTable(tag: Tag) extends Table[ShoutoutUser](tag, "USERS") {

    def id : Column[Long] = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def profilePictureUrl : Column[String] = column[String]("PROFILE_URL")

    def * = (id.?, profilePictureUrl.?) <> (ShoutoutUser.tupled, ShoutoutUser.unapply)
  }
  object shoutoutUsers extends TableQuery(new ShoutoutUserTable(_)) {
    val findUserByUrl = this.findBy(_.profilePictureUrl)
  }

  def findUsersByUrl ( url : String )= {
    db.withSession{
      implicit s =>
        shoutoutUsers.findUserByUrl(url).list()
    }
  }

  /**
   * This is a test only function to help local users configure their machines
   * and attempt to prevent them from breaking production
   */
  def testOnlyUpdateUrls() = {
    db.withSession {
      implicit s => {
        val users = shoutoutUsers.list()
        users foreach( u => {
          if(!( u.profilePictureUrl.getOrElse("") contains "-copy") ) {
            val newUrl = u.profilePictureUrl.getOrElse("").replaceAll("shoutout-prod-profile", S3Configuration.profileBucket)

            val q = for {upUser <- shoutoutUsers if upUser.id === u.id} yield upUser.profilePictureUrl

            u.profilePictureUrl match {
              case None => println("skipping this record.")
              case Some(_) => q.update(newUrl)
            }
          }
        })
      }
    }
  }

  /**
   * +-------------------+---------------+------+-----+---------+----------------+
   * | Field             | Type          | Null | Key | Default | Extra          |
   * +-------------------+---------------+------+-----+---------+----------------+
   * | ID                | int(11)       | NO   | PRI | NULL    | auto_increment |
   * | SENDER_ID         | int(11)       | NO   |     | NULL    |                |
   * | RECIPIENT_ID      | int(11)       | NO   |     | NULL    |                |
   * | TEXT              | varchar(1024) | NO   |     | NULL    |                |
   * | IMAGE_URL         | varchar(256)  | NO   |     | NULL    |                |
   * | IS_VIEWED         | tinyint(1)    | NO   |     | 0       |                |
   * | VIEWED_TIMESTAMP  | datetime      | YES  |     | NULL    |                |
   * | CREATED_TIMESTAMP | datetime      | NO   |     | NULL    |                |
   * | IS_BLOCKED        | tinyint(1)    | NO   |     | 0       |                |
   * | CONTENT_TYPE      | varchar(1024) | YES  |     | NULL    |                |
   * +-------------------+---------------+------+-----+---------+----------------+
   * @param tag
   */
  class ShoutoutTable(tag: Tag) extends Table[Shoutout](tag, "SHOUTOUTS") {

    import com.github.tototoshi.slick.MySQLJodaSupport._

    def id : Column[Long] = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def imageUrl : Column[String] = column[String]("IMAGE_URL")
    def isViewed : Column[Boolean] = column[Boolean]("IS_VIEWED")
    def viewedTimestamp : Column[LocalDate] = column[LocalDate]("VIEWED_TIMESTAMP")
    def createdTimestamp : Column[LocalDate] = column[LocalDate]("CREATED_TIMESTAMP")
    def isClean : Column[Boolean] = column[Boolean]("IS_CLEANED")
    def isBlocked : Column[Boolean] = column[Boolean]("IS_BLOCKED")

    def * = (id.?, imageUrl, isViewed, viewedTimestamp.?, createdTimestamp, isClean, isBlocked) <> (Shoutout.tupled, Shoutout.unapply)
  }
//  val shoutouts = TableQuery[ShoutoutTable]
  object shoutouts extends TableQuery(new ShoutoutTable(_)) {
    val findByUrl = this.findBy(_.imageUrl)
  }

  def findOlderThan( date : LocalDate ) : List[Shoutout] = {
    db.withSession{ implicit session : Session =>

      val q = for { s <- shoutouts if s.createdTimestamp < date && s.isClean === false } yield s
      q.list

    }
  }

  def updateAllAsCleaned( list : List[Shoutout] ) : Unit = {
    db.withSession{ implicit session : Session =>
      list foreach { item =>
        val q = for { s <- shoutouts if s.id === item.id } yield (s.isClean, s.imageUrl)
        q.update(true, "")
      }
    }
  }

  def findShoutoutsToClean() : List[ShoutoutCleanupResult] = {
    import Q.interpolation

    db.withSession { implicit session : Session =>

      implicit val getShoutoutResult = GetResult(r => ShoutoutCleanupResult(r.<<, r.<<, r.<<))

      /**
       * This query selects from two sets:
       *
       * Set 1. All the distinct S3 images that are not viewed yet and are yet to be cleaned
       * Set 2. All the distinct S3 images that have been either viewed or blocked
       *
       * The outer query provides a list of Shoutouts that are exclusively not in the first set and that are in the second set.
       * This means we will clean images only when all references to the Image in S3 have been viewed.
       */
      val cleanupQuery = sql"""SELECT ID, IMAGE_URL, IS_VIEWED, VIEWED_TIMESTAMP, CREATED_TIMESTAMP FROM SHOUTOUTS WHERE IMAGE_URL NOT IN (
        SELECT DISTINCT
          IMAGE_URL
        FROM SHOUTOUTS
        WHERE IS_VIEWED = 0 and IS_CLEANED = 0
      ) AND IMAGE_URL IN (
        SELECT DISTINCT
          IMAGE_URL
        FROM SHOUTOUTS
        WHERE (IS_VIEWED = 1 OR IS_BLOCKED = 1) and IS_CLEANED = 0
      ) AND IS_CLEANED = 0
      """.as[ShoutoutCleanupResult]
      cleanupQuery.list

    }
  }

  def findViewedWelcomeImages() : List[ShoutoutCleanupResult] = {
    import Q.interpolation

    db.withSession { implicit session : Session =>
      implicit val getShoutoutResult = GetResult(r => ShoutoutCleanupResult(r.<<, r.<<, r.<<))
      val cleanupQuery =

        sql"""select ID, IMAGE_URL, IS_VIEWED, VIEWED_TIMESTAMP, CREATED_TIMESTAMP
              from SHOUTOUTS where SENDER_ID = 1 and IS_VIEWED = 1 and IS_CLEANED = 0""".as[ShoutoutCleanupResult]

      cleanupQuery.list
    }
  }

  def findNonCleanedBlocked() : List[Shoutout] = {
    db.withSession{ implicit s : Session =>
      val q = for { s <- shoutouts if s.isBlocked === true && s.isClean === false } yield s
      q.list
    }
  }

  def findShoutoutsByUrl( url : String ) : List[Shoutout] = {
    db.withSession{ implicit s: Session =>
      shoutouts.findByUrl(url).list()
    }
  }

  /**
   * This is a test only function to help local users configure their machines
   * and attempt to prevent them from breaking production
   */
  def testOnlyShoutoutUpdateUrls() = {
    db.withTransaction {
      implicit session : Session  => {
        val shouts = shoutouts.list()
        shoutouts foreach( s => {
          if(!( s.imageUrl contains "-copy") ) {
            val newUrl = s.imageUrl.replaceAll("shoutout-prod-shouts", S3Configuration.shoutoutBucket)
            val q = for {upShoutout <- shoutouts if upShoutout.id === s.id} yield upShoutout.imageUrl
            if(!(s.imageUrl == Unit) ) {
              q.update(newUrl)
            }
          }
        })
      }
    }
  }

  /**
   * +---------------------+---------------+------+-----+---------+----------------+
   * | Field               | Type          | Null | Key | Default | Extra          |
   * +---------------------+---------------+------+-----+---------+----------------+
   * | ID                  | int(11)       | NO   | PRI | NULL    | auto_increment |
   * | ACTION_PERFORMED    | varchar(1024) | YES  |     | NULL    |                |
   * | ACTION_DATE         | datetime      | YES  |     | NULL    |                |
   * | ACTION_UPDATE_COUNT | int(11)       | YES  |     | NULL    |                |
   * +---------------------+---------------+------+-----+---------+----------------+
   */
  class JanitorStatsTable(tag: Tag) extends Table[JanitorStat](tag, "JANITOR_STATS") {

    import com.github.tototoshi.slick.MySQLJodaSupport._

    def id : Column[Long] = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def actionPerformed : Column[String] = column[String]("ACTION_PERFORMED")
    def actionDate : Column[LocalDate] = column[LocalDate]("ACTION_DATE")
    def actionUpdateCount : Column[Int] = column[Int]("ACTION_UPDATE_COUNT")

    def * = (id.?, actionPerformed, actionDate, actionUpdateCount) <> (JanitorStat.tupled, JanitorStat.unapply)
  }
  object janitorStats extends TableQuery(new JanitorStatsTable(_)) {
  }
  
  def insertJanitorStat( stat : JanitorStat ) : Boolean = {
    db.withSession{ implicit session : Session =>
      janitorStats.insert(stat) > 0
    }
  }

  /**
   * +---------------------------------+----------+------+-----+-------------------+----------------+
   * | Field                           | Type     | Null | Key | Default           | Extra          |
   * +---------------------------------+----------+------+-----+-------------------+----------------+
   * | ID                              | int(11)  | NO   | PRI | NULL              | auto_increment |
   * | PROFILE_CLEANUP                 | int(11)  | NO   |     | 0                 |                |
   * | OLD_SHOUTOUTS_CLEANUP           | int(11)  | NO   |     | 0                 |                |
   * | FULLY_VIEWED_CLEANUP            | int(11)  | NO   |     | 0                 |                |
   * | ORPHANED_SHOUTS_CLEANUP         | int(11)  | NO   |     | 0                 |                |
   * | ALLTIME_PROFILE_CLEANUP         | int(11)  | NO   |     | 0                 |                |
   * | ALLTIME_OLD_SHOUTOUTS_CLEANUP   | int(11)  | NO   |     | 0                 |                |
   * | ALLTIME_FULLY_VIEWED_CLEANUP    | int(11)  | NO   |     | 0                 |                |
   * | ALLTIME_ORPHANED_SHOUTS_CLEANUP | int(11)  | NO   |     | 0                 |                |
   * +---------------------------------+----------+------+-----+-------------------+----------------+
   */
  class JanitorFlatStatsTable(tag : Tag) extends Table[JanitorFlatStat](tag, "JANITOR_FLAT_STATS") {

    import com.github.tototoshi.slick.MySQLJodaSupport._

    def id : Column[Long] = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def profileCleanup : Column[Int] = column[Int]("PROFILE_CLEANUP")
    def oldShoutoutsCleanup : Column[Int] = column[Int]("OLD_SHOUTOUTS_CLEANUP")
    def fullyViewedCleanup : Column[Int] = column[Int]("FULLY_VIEWED_CLEANUP")
    def orphanedShoutsCleanup : Column[Int] = column[Int]("ORPHANED_SHOUTS_CLEANUP")
    def s3ImagesCleanup: Column[Int] = column[Int]("CLEANED_S3_IMAGES")
    def alltimeProfileCleanup : Column[Int] = column[Int]("ALLTIME_PROFILE_CLEANUP")
    def alltimeOldShoutoutsCleanup : Column[Int] = column[Int]("ALLTIME_OLD_SHOUTOUTS_CLEANUP")
    def alltimeFullyViewedCleanup : Column[Int] = column[Int]("ALLTIME_FULLY_VIEWED_CLEANUP")
    def alltimeOrphanedShoutsCleanup : Column[Int] = column[Int]("ALLTIME_ORPHANED_SHOUTS_CLEANUP")
    def alltimes3ImagesCleanup: Column[Int] = column[Int]("ALLTIME_CLEANED_S3_IMAGES")

    def * = (
      id.?,
      profileCleanup,
      oldShoutoutsCleanup,
      fullyViewedCleanup,
      orphanedShoutsCleanup,
      s3ImagesCleanup,
      alltimeProfileCleanup,
      alltimeOldShoutoutsCleanup,
      alltimeFullyViewedCleanup,
      alltimeOrphanedShoutsCleanup,
      alltimes3ImagesCleanup) <> (JanitorFlatStat.tupled, JanitorFlatStat.unapply)
  }

  object janitorFlatStats extends TableQuery(new JanitorFlatStatsTable(_)) {
  }

  def updateFlatStats( stat : JanitorFlatStat ) : Boolean = {
    db.withSession{ implicit session : Session =>
      janitorFlatStats.update(stat) > 0
    }
  }

  def findFlatStats() : JanitorFlatStat = {
    db.withSession{ implicit session : Session =>
      janitorFlatStats.first()
    }
  }
}

