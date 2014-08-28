import sbt._
import sbt.Keys._

object ShoutoutJanitorBuild extends Build {

  lazy val shoutoutJanitor = Project(
    id = "shoutout-janitor",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "shoutout-janitor",
      organization := "com.shoutout",
      version := "1.0",
      scalaVersion := "2.10.3",
      scalacOptions ++= Seq("-feature", "-deprecation"),

      resolvers += "spray" at "http://repo.spray.io",
      resolvers += "spray nightly" at "http://nightlies.spray.io/",
      resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
      resolvers += "jets3t" at "http://www.jets3t.org/maven2",

      libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.22",
      libraryDependencies += "com.jolbox" % "bonecp" % "0.7.1.RELEASE",
      libraryDependencies += "com.github.tototoshi" %% "slick-joda-mapper" % "1.1.0",
      libraryDependencies += "joda-time" % "joda-time" % "2.3",
      libraryDependencies += "org.joda" % "joda-convert" % "1.6",
      libraryDependencies += "net.java.dev.jets3t" % "jets3t" % "0.9.0",

      libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.1.0",
      libraryDependencies += "org.quartz-scheduler" % "quartz" % "2.1.7",
//      libraryDependencies += "com.typesafe.akka" %% "akka-quartz-scheduler" % "1.2.0-akka-2.1.x",
      libraryDependencies += "com.typesafe.slick" %% "slick" % "2.0.0",

      libraryDependencies += "org.specs2" %% "specs2" % "2.3.13" % "test"



  )
  )
}
