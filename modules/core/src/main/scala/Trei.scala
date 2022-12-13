// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

//import java.util.UUID

//import scala.concurrent.duration.*

package other

import dolphin.StoreSession
import dolphin.option.ReadOptions

import cats.effect.{IO, IOApp}
import fs2.Stream
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Trei extends IOApp.Simple {

  override def run: IO[Unit] = {
    implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]
    StoreSession
      .stream[IO]("localhost", 2113, tls = false)
      .flatMap { client =>
//        client
//          .subscribeToStream("test-stream", SubscriptionOptions.default.fromEnd)
//          .merge(Stream.awakeEvery[IO](100.milliseconds)) concurrently Stream
//          .eval(IO.pure(UUID.randomUUID()))
//          .flatMap { uuid =>
//            Stream
//              .eval(
//                client.write(
//                  "test-stream",
//                  (s"""{"test": "${uuid}"}""".getBytes, None),
//                  "test",
//                )
//              )
//          }
//          .metered(1.second)
//          .repeat

        Stream.eval(client.read("non-existing-stream", ReadOptions.default))
      }
      .compile
      .drain

  }

}
