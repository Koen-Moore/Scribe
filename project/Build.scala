package scribe

import sbt.Keys._
import sbt._

object Dependencies {
  val akka = "com.typesafe.akka" %% "akka-actor" % "2.4.0"
  val scalatest = "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"
  val slf4j = "org.slf4j" % "slf4j-api" % "1.7.5"
  val config = "com.typesafe" % "config" % "1.2.1"
  val json4sNative = "org.json4s" %% "json4s-native" % "3.3.0"
}

object Build extends Build {

  lazy val basicSettings = Seq(
    organization       := "com.bottlerocketstudios",
    name := "scribe",
    organizationName   := "Bottle Rocket Studios",
    version            := "0.6.0",
    scalaVersion       := "2.11.7",
    publishMavenStyle  := true,
    pomIncludeRepository 		  := { _ => true },
    publishArtifact in Test 	:= false,
    publishArtifact in (Compile, packageSrc) := false,
    publishArtifact in (Compile, packageDoc) := false,
    publishTo 					:= Some("Artifactory Realm" at "https://artifacts.bottlerocketservices.com/repository/server-development-local/")
  )

  lazy val additionalSettings = Seq(
    scalacOptions := Seq(
      "-deprecation",
      "-encoding", "UTF-8",
      "-feature",
      "-language:existentials",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-unchecked",
      "-Xfatal-warnings",
      "-Xlint",
      "-Yno-adapted-args",
      "-Xfuture"
    )
  )

  lazy val loggerSettings = {
    def addFileToResourceGenerators(filename: String, config: Configuration) = {
      resourceGenerators in config <+= {
        baseDirectory in config map { _ =>
          Seq(new File(filename))
        }
      }
    }

    addFileToResourceGenerators("logback.xml", sbt.Compile) ++ addFileToResourceGenerators("logback-test.xml", sbt.Test)
  }

  val artifactoryCredentials = {
    val envVariableName = "_ARTIFACTORY_CREDENTIALS" // name used on other jenkins builds within BR for injecting credentials
    scala.util.Properties.envOrNone(envVariableName) match {
      case Some(creds) =>
        val usernamePass = creds.split(":")
        Credentials("Artifactory Realm", "artifacts.bottlerocketservices.com", usernamePass(0), usernamePass(1))
      case None =>
        Credentials(Path.userHome / ".sbt" / ".artifactory_credentials")
    }
  }

  lazy val resolverSettings =
    Seq(resolvers +=  "ServerDevelopment" at "https://artifacts.bottlerocketservices.com/repository/server-development/",
    resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
  )

  override lazy val settings =
    super.settings ++
      basicSettings ++
      resolverSettings ++
      Seq(
        credentials := Seq(artifactoryCredentials),
        shellPrompt := { s => Project.extract(s).currentProject.id + " > " }
      )

  lazy val parentSettings = Defaults.coreDefaultSettings ++
    Defaults.itSettings ++
    loggerSettings ++
    additionalSettings

  import Dependencies._
  lazy val root = Project(
    id = "scribe",
    base = file("."),
    settings = parentSettings ++ settings) settings (
    libraryDependencies ++= Seq(scalatest,akka,slf4j,json4sNative)
  )

  implicit class ProjectExtensions(project: Project) {
    def withTestPackages = project % "compile->compile;test->test"
  }
}
