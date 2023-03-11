import sbt._

object Dependencies {

  object V {

    val betterMonadicFor   = "0.3.1"
    val catsCore           = "2.9.0"
    val catsEffect         = "3.4.8"
    val circe              = "0.14.4"
    val fs2                = "3.6.1"
    val eventStoreDbClient = "4.1.1"
    val kindProjector      = "0.13.2"
    val log4cats           = "2.5.0"
    val logback            = "1.4.5"
    val organizeImports    = "0.6.0"
    val semanticDB         = "4.7.4"
    val weaver             = "0.8.1"

  }

  object Libraries {

    def circe(artifact: String): ModuleID = "io.circe" %% s"circe-$artifact" % V.circe

    val catsCore: ModuleID           = "org.typelevel" %% "cats-core"      % V.catsCore
    val catsEffect: ModuleID         = "org.typelevel" %% "cats-effect"    % V.catsEffect
    val circeCore: ModuleID          = circe("core")
    val circeParser: ModuleID        = circe("parser")
    val fs2Core: ModuleID            = "co.fs2"        %% "fs2-core"       % V.fs2
    val eventStoreDbClient: ModuleID = "com.eventstore" % "db-client-java" % V.eventStoreDbClient
    val sourceCode: ModuleID         = "com.lihaoyi"   %% "sourcecode"     % "0.3.0"

    val log4cats: ModuleID        = "org.typelevel"        %% "log4cats-slf4j"   % V.log4cats
    val logback: ModuleID         = "ch.qos.logback"        % "logback-classic"  % V.logback
    val organizeImports: ModuleID = "com.github.liancheng" %% "organize-imports" % V.organizeImports

    val catsLaws         = "org.typelevel"       %% "cats-laws"         % V.catsCore
    val log4catsNoOp     = "org.typelevel"       %% "log4cats-noop"     % V.log4cats
    val weaverCats       = "com.disneystreaming" %% "weaver-cats"       % V.weaver
    val weaverDiscipline = "com.disneystreaming" %% "weaver-discipline" % V.weaver
    val weaverScalaCheck = "com.disneystreaming" %% "weaver-scalacheck" % V.weaver
  }

  object CompilerPlugin {

    val betterMonadicFor: ModuleID = compilerPlugin(
      "com.olegpy" %% "better-monadic-for" % V.betterMonadicFor
    )

    val kindProjector: ModuleID = compilerPlugin(
      "org.typelevel" % "kind-projector" % V.kindProjector cross CrossVersion.full
    )

    val semanticDB: ModuleID = compilerPlugin(
      "org.scalameta" % "semanticdb-scalac" % V.semanticDB cross CrossVersion.full
    )

  }
}
