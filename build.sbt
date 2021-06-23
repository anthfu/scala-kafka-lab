ThisBuild / organization := "com.anthfu"
ThisBuild / version := "1.0.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.6"

lazy val zioVersion = "1.0.9"
lazy val zioConfigVersion = "1.0.6"
lazy val zioKafkaVersion = "0.15.0"

lazy val commonSettings = Seq(
  libraryDependencies ++= Seq(
    "dev.zio" %% "zio-config"      % zioConfigVersion,
    "dev.zio" %% "zio-config-yaml" % zioConfigVersion,
    "dev.zio" %% "zio-kafka"       % zioKafkaVersion,
    "dev.zio" %% "zio-streams"     % zioVersion
  )
)

lazy val `zio-consumer` = project
  .settings(commonSettings)

lazy val `zio-producer` = project
  .settings(commonSettings)
