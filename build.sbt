import Dependencies._

ThisBuild / tlBaseVersion := "0.1"

ThisBuild / scalaVersion               := "2.13.9"
ThisBuild / startYear                  := Some(2022)
ThisBuild / scalafixDependencies ++= Seq(Libraries.organizeImports)
ThisBuild / organization               := "com.lapsus"
ThisBuild / organizationName           := "Lapsus"
ThisBuild / licenses                   := Seq(License.MIT)
ThisBuild / developers                 := List(
  tlGitHubDev("samgj18", "Samuel Gomez")
)
ThisBuild / semanticdbVersion          := scalafixSemanticdb.revision
ThisBuild / semanticdbEnabled          := true
ThisBuild / tlJdkRelease               := Some(17)
ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.temurin("17"))
ThisBuild / tlCiReleaseBranches        := Seq("main")

lazy val commonSettings = Seq(
  resolvers ++= Resolver.sonatypeOssRepos("snapshots"),
  scalacOptions ++= Seq(
    "-Ymacro-annotations",
    "-Xsource:3",
    "-Yrangepos",
    "-Wconf:cat=unused:error",
    "-deprecation",
  ),
)

lazy val dolphin = tlCrossRootProject
  .settings(commonSettings)
  .aggregate(core, circe)
  .settings(
    name := "dolphin"
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
