// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.util

import com.eventstore.dbclient.*
import sourcecode.{File, Line}

private[util] object PrettyPrinter {

  def beautify(error: Throwable, msg: Option[String] = None)(implicit line: Line, file: File): String = {
    val message  = Option(error.getMessage).getOrElse(error.getClass.getSimpleName)
    val metadata = {
      val stackTraceString = error
        .getStackTrace
        .map { element =>
          s"  at ${element.getClassName}.${element.getMethodName}(${element.getFileName}:${element.getLineNumber})"
        }
        .mkString("\n")

//
      s"""
        |Stack trace:
        |${msg.getOrElse("")}
        |ℹ️ maybe ${file.value}:${line.value} ℹ️
        |$stackTraceString
        |""".stripMargin

      // beautify stack trace

    }
    val hint =
      error match {
        case _: StreamNotFoundException          => "The stream you are trying to read from does not exist"
        case _: NotLeaderException               => "The request needing a leader node was executed on a follower node."
        case _: ResourceNotFoundException        =>
          "The resource you are trying to access does not exist or you have no access. Could only happen when a request was performed through HTTP."
        case _: NoClusterNodeFoundException      => "No cluster node found on the provided connection string."
        case _: ConnectionShutdownException      =>
          "You are trying to perform an operation on a connection that has been shutdown."
        case _: UnsupportedFeatureException      => "The feature you are trying to use is not supported by the server."
        case _: WrongExpectedVersionException    =>
          "The expected version you provided does not match the current version of the stream, this can happen when relying on optimistic concurrency."
        case _: ConnectionStringParsingException =>
          "The connection string you provided is not valid. Please check the documentation for more information."
        case t: Throwable                        => s"An unexpected error occurred: no hint available for this error: ${t.getClass.getName}"
      }
    s"""\n
       |🔥${message}🔥
       |❇️${hint}❇️
       |${metadata}
       |\n
       |""".stripMargin
  }

}
