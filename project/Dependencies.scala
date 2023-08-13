import sbt._

object Dependencies {

  object V {

    val betterMonadicFor   = "0.3.1"
    val catsCore           = "2.9.0"
    val catsEffect         = "3.5.1"
    val fs2                = "3.8.0"
    val eventStoreDbClient = "4.3.0"
    val kindProjector      = "0.13.2"
    val log4cats           = "2.6.0"
    val logback            = "1.4.11"
    val organizeImports    = "0.6.0"
    val semanticDB         = "4.8.7"
    val weaver             = "0.8.3"

  }

  object Libraries {
    val catsCore: ModuleID           = "org.typelevel" %% "cats-core"      % V.catsCore
    val catsEffect: ModuleID         = "org.typelevel" %% "cats-effect"    % V.catsEffect
    val fs2Core: ModuleID            = "co.fs2"        %% "fs2-core"       % V.fs2
    val eventStoreDbClient: ModuleID = "com.eventstore" % "db-client-java" % V.eventStoreDbClient
    val sourceCode: ModuleID         = "com.lihaoyi"   %% "sourcecode"     % "0.3.0"

    val log4cats: ModuleID        = "org.typelevel"        %% "log4cats-slf4j"   % V.log4cats
    val logback: ModuleID         = "ch.qos.logback"        % "logback-classic"  % V.logback
    val organizeImports: ModuleID = "com.github.liancheng" %% "organize-imports" % V.organizeImports

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
