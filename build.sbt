ThisBuild / organization := "com.anthfu"
ThisBuild / scalaVersion := "2.13.6"
ThisBuild / version := "1.0.0-SNAPSHOT"

lazy val kafkaVersion = "2.8.0"
lazy val zioVersion = "1.0.9"

lazy val commonSettings = Seq(
  libraryDependencies ++= Seq(
    "dev.zio" %% "zio-kafka" % "0.15.0",
    "dev.zio" %% "zio-streams" % zioVersion,
    "org.apache.kafka" % "kafka-clients" % kafkaVersion
  )
)

lazy val `zio-consumer` = project
  .settings(commonSettings)

lazy val `zio-producer` = project
  .settings(commonSettings)
