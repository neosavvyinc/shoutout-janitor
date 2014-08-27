package com.shoutout.db

/**
 * Created by aparrish on 8/27/14.
 */
package object repository {

//  import scala.slick.driver.MySQLDriver.simple._
//  import com.github.tototoshi.slick.MySQLJodaSupport._
//  import scala.slick.jdbc.{GetResult, StaticQuery => Q}

//
//  object DBConfiguration {
//    val statementCacheSize = 50
//    val minConnectionsPerPartition = 100
//    val maxConnectionsPerPartition = 100
//    val numPartitions = 1
//
//    val driver = "com.mysql.jdbc.Driver"
//    val url = "jdbc:mysql://localhost/cadencev1"
//    val user = "cadencev1"
//    val pass = "cadencev1"
//  }

//  val db = Database.forURL( DBConfiguration.url,
//    driver = DBConfiguration.driver,
//    user = DBConfiguration.user,
//    password = DBConfiguration.pass)

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
//  class CadenceUserTable(tag: Tag) extends Table[CadenceUser](tag, "USERS") {
//
//    def id: Column[Int] = column[Int]("ID", O.PrimaryKey, O.AutoInc)
//    def firstName : Column[String] = column[String]("FIRST_NAME")
//    def lastName : Column[String] = column[String]("LAST_NAME")
//    def email: Column[String] = column[String]("EMAIL")
//    def company: Column[String] = column[String]("COMPANY")
//    def password: Column[String] = column[String]("PASSWORD")
//
//    def * = (id.?, firstName, lastName, email, company, password) <> (CadenceUser.tupled, CadenceUser.unapply)
//  }
//  val cadenceUsers = TableQuery[CadenceUserTable]
//  object cadenceUsersExt extends TableQuery(new CadenceUserTable(_)) {
//    // put extra methods here, e.g.:
//    val findByID = this.findBy(_.id)
//  }



}

