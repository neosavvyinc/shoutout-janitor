package com.shoutout.db

import java.util.UUID

import org.joda.time.{LocalDate, DateTime}

import scala.slick.ast.ColumnOption.DBType
import scala.slick.direct.AnnotationMapper.column
import scala.slick.lifted.ProvenShape


/**
 * Created by aparrish on 8/27/14.
 */
package object repository {

  import scala.slick.driver.MySQLDriver.simple._
  import com.github.tototoshi.slick.MySQLJodaSupport._
  import scala.slick.jdbc.{GetResult, StaticQuery => Q}


  object DBConfiguration {
    val statementCacheSize = 50
    val minConnectionsPerPartition = 100
    val maxConnectionsPerPartition = 100
    val numPartitions = 1

    val driver = "com.mysql.jdbc.Driver"
    val url = "jdbc:mysql://localhost/shoutout"
    val user = "root"
    val pass = ""
  }

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

  def testOnlyUpdateUrls() = {
    db.withSession {
      implicit s => {
        val users = shoutoutUsers.list()
        users foreach( u => {
          if(!( u.profilePictureUrl.get contains "-copy") ) {
            val newUrl = u.profilePictureUrl.getOrElse("").replaceAll("shoutout-prod-profile", "shoutout-prod-profile-copy")

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

    def * = (id.?, imageUrl, isViewed, viewedTimestamp.?, createdTimestamp) <> (Shoutout.tupled, Shoutout.unapply)
  }
  val shoutouts = TableQuery[ShoutoutTable]

  def findOlderThan( date : LocalDate ) : List[Shoutout] = {
    db.withSession{ implicit session : Session =>
      val q = shoutouts.filter( _.createdTimestamp < date )
      println(q.selectStatement)
      q.list
    }
  }

  def deleteAll( list : List[Shoutout] ) : Unit = {
    db.withSession{ implicit session : Session =>
      list foreach( item => shoutouts.filter(_.id === item.id).delete )
    }
  }

  def testOnlyShoutoutUpdateUrls() = {
    db.withTransaction {
      implicit session : Session  => {
        val shouts = shoutouts.list()
        shoutouts foreach( s => {
          if(!( s.imageUrl contains "-copy") ) {
            val newUrl = s.imageUrl.replaceAll("shoutout-prod-shouts", "shoutout-prod-shouts-copy")
            val q = for {upShoutout <- shoutouts if upShoutout.id === s.id} yield upShoutout.imageUrl
            if(!(s.imageUrl == Unit) ) {
              q.update(newUrl)
            }
          }
        })
      }
    }
  }

}

