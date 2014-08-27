import sbt._
import sbt.Keys._

object ShoutoutJanitorBuild extends Build {

  lazy val shoutoutJanitor = Project(
    id = "shoutout-janitor",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "Shoutout Janitorial Services",
      organization := "com.shoutout",
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.10.0",
      scalacOptions ++= Seq("-feature", "-deprecation"),
      resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",

      libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.1.0",
      libraryDependencies += "com.typesafe.akka" %% "akka-quartz-scheduler" % "1.2.0-akka-2.1.x"

//      libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % "2.2.3",
//      libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.13",
//
//      libraryDependencies += "com.typesafe" %%  "scalalogging-slf4j" % "1.0.1"

    )
  )
}
