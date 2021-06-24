ThisBuild / organization := "com.anthfu"
ThisBuild / version := "1.0.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.6"

lazy val zioSettings = {
  lazy val zioVersion = "1.0.9"
  lazy val zioConfigVersion = "1.0.6"
  lazy val zioHttpVersion = "1.0.0.0-RC17"
  lazy val zioKafkaVersion = "0.15.0"

  Seq(
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-config"      % zioConfigVersion,
      "dev.zio" %% "zio-config-yaml" % zioConfigVersion,
      "dev.zio" %% "zio-kafka"       % zioKafkaVersion,
      "dev.zio" %% "zio-streams"     % zioVersion,
      "io.d11"  %% "zhttp"           % zioHttpVersion
    )
  )
}

lazy val `zio-consumer` = project
  .settings(zioSettings)

lazy val `zio-producer` = project
  .settings(zioSettings)
