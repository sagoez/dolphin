import Dependencies._

ThisBuild / tlBaseVersion := "0.0"

ThisBuild / scalaVersion               := "2.13.12"
ThisBuild / startYear                  := Some(2022)
ThisBuild / scalafixDependencies ++= Seq(Libraries.organizeImports)
ThisBuild / organization               := "io.github.lapsushq"
ThisBuild / licenses                   := Seq(License.MIT)
ThisBuild / tlSonatypeUseLegacyHost    := false
ThisBuild / developers                 := List(
  tlGitHubDev("samgj18", "Samuel Gomez")
)
ThisBuild / semanticdbVersion          := scalafixSemanticdb.revision
ThisBuild / semanticdbEnabled          := true
ThisBuild / tlJdkRelease               := Some(17)
ThisBuild / tlCiReleaseBranches        := Seq("main")
ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.temurin("17"))

lazy val commonSettings = Seq(
  resolvers ++= Resolver.sonatypeOssRepos("snapshots"),

  // Headers
  headerMappings    := headerMappings.value + (HeaderFileType.scala -> HeaderCommentStyle.cppStyleLineComment),
  headerLicense     := Some(
    HeaderLicense.Custom(
      """|Copyright (c) 2022 by LapsusHQ
       |This software is licensed under the MIT License (MIT).
       |For more information see LICENSE or https://opensource.org/licenses/MIT
       |""".stripMargin
    )
  ),
  scalacOptions ++= Seq(
    "-Ymacro-annotations",
    "-Xsource:3",
    "-Yrangepos",
    "-Wconf:cat=unused:error",
    "-deprecation"
  ),
  scalafmtOnCompile := false,
  autoAPIMappings   := true
)

ThisBuild / githubWorkflowBuildPreamble ++=
  List(
    // Docker compose up
    WorkflowStep.Run(
      List(
        "docker-compose up -d"
      ),
      name = Some("Starting up EventStoreDB 🐳")
    ),
    WorkflowStep.Sbt(List("it"), name = Some("Integration tests 🧪"))
  )

ThisBuild / githubWorkflowBuildPostamble ++= List(
  // Docker compose down
  WorkflowStep.Run(
    List(
      "docker-compose down"
    ),
    name = Some("Stopping EventStoreDB 🐳")
  )
)

lazy val dolphin = tlCrossRootProject
  .settings(commonSettings)
  .aggregate(core, tests)
  .settings(
    name := "dolphin"
  )

lazy val core = project
  .in(file("modules/core"))
  .enablePlugins(AutomateHeaderPlugin)
  .settings(commonSettings)
  .settings(
    name := "dolphin-core",
    libraryDependencies ++= Seq(
      CompilerPlugin.betterMonadicFor,
      CompilerPlugin.kindProjector,
      CompilerPlugin.semanticDB,
      Libraries.catsCore,
      Libraries.catsEffect,
      Libraries.eventStoreDbClient,
      Libraries.fs2Core,
      Libraries.log4cats,
      Libraries.logback % Runtime,
      Libraries.sourceCode
    )
  )

lazy val tests = project
  .in(file("modules/tests"))
  .configs(IntegrationTest)
  .settings(commonSettings)
  .dependsOn(core)
  .enablePlugins(AutomateHeaderPlugin, NoPublishPlugin)
  .settings(
    scalacOptions -= "-Xfatal-warnings",
    libraryDependencies ++= Seq(
      Libraries.log4catsNoOp,
      Libraries.weaverCats,
      Libraries.weaverDiscipline,
      Libraries.weaverScalaCheck
    ),
    Defaults.itSettings,
    testFrameworks += new TestFramework("weaver.framework.CatsEffect")
  )

addCommandAlias("lint", "scalafmtAll; scalafixAll --rules OrganizeImports; scalafmtSbt; headerCreateAll")
addCommandAlias(
  "build",
  "clean; all scalafmtCheckAll scalafmtSbtCheck compile test doc"
)
addCommandAlias("it", "clean; all scalafmtCheckAll scalafmtSbtCheck it:compile it:test")
