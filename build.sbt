import Dependencies._

ThisBuild / scalaVersion      := "2.13.9"
ThisBuild / version           := "0.0.1"
ThisBuild / scalafixDependencies ++= Seq(Libraries.organizeImports)
ThisBuild / organization      := "com.lapsus"
ThisBuild / licenses          := Seq(License.MIT)
ThisBuild / developers        := List(
  Developer("lapsusHQ", "Samuel", "sgomezj18@gmail.com", url("https://samuelgomez.co/"))
)
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision
ThisBuild / semanticdbEnabled := true

lazy val commonSettings = Seq(
  // Resolvers
  resolvers ++= Resolver.sonatypeOssRepos("snapshots"),

  // Headers
  headerMappings := headerMappings.value + (HeaderFileType.scala -> HeaderCommentStyle.cppStyleLineComment),
  headerLicense  := Some(
    HeaderLicense.Custom(
      """|Copyright (c) 2022 by Samuel Gomez
       |This software is licensed under the MIT License (MIT).
       |For more information see LICENSE or https://opensource.org/licenses/MIT
       |""".stripMargin
    )
  ),

  // Compilation
  scalacOptions ++= Seq(
    "-Ymacro-annotations",
    "-Xsource:3",
    "-Yrangepos",
    "-Wconf:cat=unused:error",
    "-deprecation",
  ),
)

lazy val dolphin = project
  .in(file("."))
  .settings(
    name := "dolphin",
    commonSettings,
  )
  .aggregate(
    core,
    circe,
  )

lazy val circe = project
  .in(file("modules/circe"))
  .settings(commonSettings)
  .settings(
    name := "dolphin-circe",
    libraryDependencies ++= Seq(
      Libraries.circeCore,
      Libraries.circeGeneric,
      Libraries.circeParser,
      Libraries.circeGenericExtras,
      Libraries.circeScodec,
      Libraries.scodecBits,
    ),
  )
  .dependsOn(core)

lazy val core = project
  .in(file("modules/core"))
  .settings(commonSettings)
  .settings(
    name := "dolphin-core",
    libraryDependencies ++= Seq(
      Dependencies.CompilerPlugin.betterMonadicFor,
      Dependencies.CompilerPlugin.kindProjector,
      Dependencies.CompilerPlugin.semanticDB,
      Libraries.catsCore,
      Libraries.catsEffect,
      Libraries.eventStoreDbClient,
      Libraries.fs2Core,
      Libraries.log4cats,
      Libraries.logback,
    ),
  )

addCommandAlias("lint", "scalafmtAll; scalafixAll --rules OrganizeImports; scalafmtSbt")
addCommandAlias(
  "build",
  "clean; all scalafmtCheckAll scalafmtSbtCheck compile test doc",
)
