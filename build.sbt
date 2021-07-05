ThisBuild / organization := "com.anthfu"
ThisBuild / version := "1.0.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.6"

lazy val commonSettings = Seq(
  scalacOptions += "-target:11",
  libraryDependencies ++= Seq(
    "ch.qos.logback" % "logback-classic" % "1.2.3"
  )
)

lazy val zioSettings = {
  lazy val zioVersion = "1.0.9"
  lazy val zioConfigVersion = "1.0.6"

  Seq(
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio"               % zioVersion,
      "dev.zio" %% "zio-config"        % zioConfigVersion,
      "dev.zio" %% "zio-config-yaml"   % zioConfigVersion,
      "dev.zio" %% "zio-kafka"         % "0.15.0",
      "dev.zio" %% "zio-logging-slf4j" % "0.5.11",
      "dev.zio" %% "zio-streams"       % zioVersion,
      "io.d11"  %% "zhttp"             % "1.0.0.0-RC17"
    )
  )
}

lazy val testSettings = {
  lazy val testcontainersScalaVersion = "0.39.5"

  libraryDependencies ++= Seq(
    "com.dimafeng"  %% "testcontainers-scala-kafka" % testcontainersScalaVersion % Test,
    "com.dimafeng"  %% "testcontainers-scala-scalatest" % testcontainersScalaVersion % Test,
    "org.scalatest" %% "scalatest" % "3.2.9" % Test
  )
}

lazy val dockerSettings = Seq(
  dockerBaseImage := "adoptopenjdk:11-jre-hotspot",
  dockerExposedPorts ++= Seq(8080),
  dockerUpdateLatest := true
)

lazy val `zio-consumer` = project
  .enablePlugins(DockerPlugin, JavaAppPackaging)
  .settings(commonSettings ++ zioSettings ++ dockerSettings)

lazy val `zio-producer` = project
  .enablePlugins(DockerPlugin, JavaAppPackaging)
  .settings(commonSettings ++ zioSettings ++ dockerSettings)

lazy val `integration-test` = project
  .dependsOn(`zio-consumer`, `zio-producer`)
  .settings(commonSettings ++ testSettings)
