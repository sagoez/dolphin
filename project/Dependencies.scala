import sbt._

object Dependencies {

  object V {

    val betterMonadicFor   = "0.3.1"
    val catsCore           = "2.9.0"
    val catsEffect         = "3.4.2"
    val circe              = "0.14.3"
    val fs2                = "3.4.0"
    val eventStoreDbClient = "4.0.0"
    val kindProjector      = "0.13.2"
    val log4cats           = "2.5.0"
    val logback            = "1.4.5"
    val organizeImports    = "0.6.0"
    val semanticDB         = "4.6.0"
    val scodecBits         = "1.1.34"

  }

  object Libraries {

    def circe(artifact: String) = "io.circe" %% s"circe-$artifact" % V.circe

    val catsCore           = "org.typelevel" %% "cats-core"      % V.catsCore
    val catsEffect         = "org.typelevel" %% "cats-effect"    % V.catsEffect
    val circeCore          = circe("core")
    val circeGeneric       = circe("generic")
    val circeGenericExtras = circe("generic-extras")
    val circeParser        = circe("parser")
    val circeScodec        = circe("scodec")
    val fs2Core            = "co.fs2"        %% "fs2-core"       % V.fs2
    val eventStoreDbClient = "com.eventstore" % "db-client-java" % V.eventStoreDbClient
    val scodecBits         = "org.scodec"    %% "scodec-bits"    % V.scodecBits

    val log4cats        = "org.typelevel"        %% "log4cats-slf4j"   % V.log4cats
    val logback         = "ch.qos.logback"        % "logback-classic"  % V.logback
    val organizeImports = "com.github.liancheng" %% "organize-imports" % V.organizeImports
  }

  object CompilerPlugin {

    val betterMonadicFor = compilerPlugin(
      "com.olegpy" %% "better-monadic-for" % V.betterMonadicFor
    )

    val kindProjector = compilerPlugin(
      "org.typelevel" % "kind-projector" % V.kindProjector cross CrossVersion.full
    )

    val semanticDB = compilerPlugin(
      "org.scalameta" % "semanticdb-scalac" % V.semanticDB cross CrossVersion.full
    )

  }
}
