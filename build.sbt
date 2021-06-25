ThisBuild / organization := "com.anthfu"
ThisBuild / version := "1.0.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.6"

lazy val commonSettings = Seq(
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

lazy val `zio-consumer` = project
  .settings(commonSettings ++ zioSettings)

lazy val `zio-producer` = project
  .settings(commonSettings ++ zioSettings)
