import sbt._
import Keys._

object Yajq extends Build {
  lazy val core: Project = Project(
    id = "yajq-core",
    base = file("core"),
    settings = commonSettings
  )

  /*lazy val web: Project = Project(
    id = "yajq-web",
    base = file("web"),
    dependencies = Seq(core),
    settings = commonSettings
  )*/

  def commonSettings = Defaults.defaultSettings ++ Seq(
    organization := "edu.umd.mith",
    version := "0.0.0-SNAPSHOT",
    scalaVersion := "2.10.2",
    resolvers ++= Seq(
      Resolver.sonatypeRepo("snapshots"),
      "spray" at "http://repo.spray.io/",
      "rediscala" at "https://github.com/etaty/rediscala-mvn/raw/master/releases/"
    ),
    scalacOptions := Seq(
      "-feature",
      "-deprecation",
      "-language:postfixOps",
      "-unchecked"
    ),
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-simple" % "1.6.4",
      "io.spray" %% "spray-json" % "1.2.5",
      "com.etaty.rediscala" %% "rediscala" % "1.0"
    )
  )
}

