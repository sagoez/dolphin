// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.concurrent

import scala.jdk.OptionConverters.RichOptional

sealed trait StreamPosition[+A] extends Product with Serializable

object StreamPosition {
  case object Start extends StreamPosition[Nothing]
  case object End   extends StreamPosition[Nothing]

  final case class Exact[A](value: A) extends StreamPosition[A]

  final implicit class StreamPositionOps[A](private val self: StreamPosition[A]) extends AnyVal {

    private[dolphin] def toJava: com.eventstore.dbclient.StreamPosition[A] =
      self match {
        case Start           => com.eventstore.dbclient.StreamPosition.start()
        case End             => com.eventstore.dbclient.StreamPosition.end()
        case value: Exact[A] => com.eventstore.dbclient.StreamPosition.position(value.value)
      }
  }

  final implicit class StreamPositionJavaOps[A](private val self: com.eventstore.dbclient.StreamPosition[A])
    extends AnyVal {

    private[dolphin] def toScala: StreamPosition[A] =
      if (self.isStart)
        Start
      else if (self.isEnd)
        End
      else {
        self.getPosition.toScala.map(Exact.apply).getOrElse(Start)
      }
  }
}
