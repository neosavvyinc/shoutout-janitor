import sbt._
import sbt.Keys._

object ShoutoutJanitorServicesBuild extends Build {

  lazy val shoutoutJanitor = Project(
    id = "akka-book",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "Shoutout Janitorial Services",
      organization := "com.shoutout",
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.10.0",
      scalacOptions ++= Seq("-feature", "-deprecation"),
      resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
      libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.1.0"
    )
  )
}
