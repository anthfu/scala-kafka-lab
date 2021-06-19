ThisBuild / organization := "com.anthfu"
ThisBuild / scalaVersion := "2.13.6"
ThisBuild / version := "1.0.0-SNAPSHOT"

lazy val commonSettings = Seq(
  libraryDependencies ++= Seq(
    "dev.zio" %% "zio-streams" % "1.0.9",
    "dev.zio" %% "zio-kafka"   % "0.15.0"
  )
)

lazy val `zio-consumer` = project
  .settings(commonSettings)

lazy val `zio-producer` = project
  .settings(commonSettings)
