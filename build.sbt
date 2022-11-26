scalaVersion := "2.13.9"

name         := "event-store-db-demo"
organization := "ch.epfl.scala"
version      := "1.0"

libraryDependencies ++= Seq(
  "com.eventstore" % "db-client-java"        % "4.0.0",
  "org.typelevel" %% "cats-core"             % "2.8.0",
  "org.typelevel" %% "cats-effect"           % "3.3.14",
  "org.scodec"    %% "scodec-bits"           % "1.1.34",
  "io.circe"      %% s"circe-core"           % "0.14.1",
  "io.circe"      %% s"circe-generic"        % "0.14.1",
  "io.circe"      %% s"circe-generic-extras" % "0.14.1",
  "io.circe"      %% s"circe-parser"         % "0.14.1",
  "io.circe"      %% s"circe-scodec"         % "0.14.1",
)

scalacOptions ++= Seq(
  "-Ymacro-annotations",
  "-Xsource:3",
  "-Yrangepos",
  "-deprecation",
  "-Ywarn-dead-code",
  "-Xfatal-warnings",
)
