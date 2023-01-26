# Dolphin<a href="https://typelevel.org/cats/"><img src="https://typelevel.org/cats/img/cats-badge.svg" height="40px" align="right" alt="Cats friendly" />

[![Continuous Integration](https://github.com/lapsusHQ/dolphin/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/lapsusHQ/dolphin/actions/workflows/ci.yml)
[![Clean](https://github.com/lapsusHQ/dolphin/actions/workflows/clean.yml/badge.svg)](https://github.com/lapsusHQ/dolphin/actions/workflows/clean.yml)
![GitHub issues](https://img.shields.io/github/issues/lapsusHQ/dolphin)

## Introduction

EventStoreDB is an open-source state-transition database, designed for businesses that are ready to harness the true
power of event-driven architecture. It is a purpose-built database for event-driven applications, with a focus on high
performance, scalability, and reliability.

## ⚠️ Disclaimer

Dolphin is a Scala wrapper for the Java client of EventStoreDB. It is a work in progress and is not ready nor recommended for production use.

## Table of Contents

- [Dolphin](#dolphin)
  - [Introduction](#introduction)
  - [⚠️ Disclaimer](#️-disclaimer)
  - [Table of Contents](#table-of-contents)
  - [Installation](#installation)
  - [Usage](#usage)
    - [Creating a volatile session](#creating-a-volatile-session)
    - [Creating a persistent session](#creating-a-persistent-session)
    - [Reading and appending to a stream](#reading-and-appending-to-a-stream)
    - [Subscribing to a stream _with stream handler_](#subscribing-to-a-stream-with-stream-handler)
  - [Roadmap](#roadmap)
  - [Note](#note)

## Installation

Add the following to your `build.sbt` file:

```scala
libraryDependencies += "io.github.lapsushq" %% "dolphin-core" % "0.0-`Latest Commit Hash`-SNAPSHOT"
```

## Usage

### Creating a volatile session

```scala
import cats.effect.IO
import dolphin.VolatileSession
import dolphin.setting.EventStoreSettings

val session = VolatileSession.stream[IO](EventStoreSettings.Default)
```

### Creating a persistent session

```scala
import cats.effect.IO
import dolphin.PersistentSession
import dolphin.setting.EventStoreSettings

val session = PersistentSession.stream[IO](EventStoreSettings.Default)
```

### Reading and appending to a stream

```scala
import cats.effect.{IO, IOApp}
import dolphin.VolatileSession
import dolphin.setting.{EventStoreSettings, ReadFromStreamSettings}
import fs2.Stream
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends IOApp.Simple {

  implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  override def run: IO[Unit] =
    (for {
      session <- VolatileSession.stream[IO](EventStoreSettings.Default)
      _       <- Stream.eval(
        session.appendToStream("test-stream", """{"hello": "world"}""".getBytes, Array.emptyByteArray, "test")
      )
      read    <- Stream.eval(session.readStream("test-stream", ReadFromStreamSettings.Default))
      data    <- read.getEventData
      _       <- Stream.eval(IO.println(new String(data))) // {"hello": "world"}
    } yield ())
      .compile
      .drain
}
```

#### Subscribing to a stream _with stream handler_

```scala
import dolphin.*
import cats.effect.{IO, IOApp}
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import fs2.Stream

import java.util.UUID
import scala.concurrent.duration.*

object Main extends IOApp.Simple {

  implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  def program: Stream[IO, Unit] =
    for {
      session <- VolatileSession.stream[IO](Config.default)
      _       <- Stream
              .iterateEval(UUID.randomUUID())(_ => IO(UUID.randomUUID()))
              .evalMap { uuid =>
                session
                        .appendToStream(
                          "cocomono",
                          s"""{"test": "${uuid}"}""".getBytes,
                          Array.emptyByteArray,
                          "test"
                        )
              }
              .metered(10.seconds)
              .concurrently {
                session.subscribeToStream("cocomono").evalMap {
                  case Message.Event(_, event, _) => event.getEventData.map(new String(_)).flatMap(logger.info(_))
                  case Message.Error(_, error) => logger.error(s"Received error: ${error}")
                  case Message.Cancelled(_)    => logger.info("Received cancellation")
                  case Message.Confirmation(_) => logger.info("Received confirmation")
                }
              }
    } yield ()

  override def run: IO[Unit] = program.compile.drain

}
```

### Subscribing to a stream _with handler_

```scala
import dolphin.*

import cats.effect.{IO, IOApp}
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends IOApp.Simple {

  implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  private val handlers: Message[IO, VolatileConsumer[IO]] => IO[Unit] = {
    case Message.Event(consumer, event, retryCount) =>
      logger.info(s"Received event: $event")
    case Message.Error(consumer, error) =>
      logger.error(error)(s"Received error: $error")
    case Message.Cancelled(consumer)    =>
      logger.info(s"Received cancellation")
    case Message.Confirmation(consumer) =>
      logger.info(s"Received confirmation")
  }

  override def run: IO[Unit] =
    (for {
      session <- VolatileSession.resource[IO](Config.default)
      _       <- session.subscribeToStream("serial", handlers)

    } yield ()).useForever

}
```

## Roadmap

- [x] Add a docker-compose file to run EventStoreDB.
- [x] Figure out what to do with some result data types like Position, ExpectedVersion.
- [x] [Write a simple application that uses this wrapper](https://github.com/samgj18/event-sourcing-poc/)
- [x] Keep the wrapper up to date with the latest version of the Java client.
- [x] Revisit if we should log the errors or not simply let the user handle logging.
- [x] Write tests for Client, Session, StoreSession and Trace.
- [x] Write all the missing data types and methods in the wrapper.
- [x] Resolve authentication handling.
- [ ] Write documentation on how to use the wrapper.
- [ ] Revisit design decisions and refactor if needed.
- [ ] Keeping the session open for the whole application lifetime is not ideal since it seems to starve the cpu.
- [ ] Improve the way we handle the subscription listener.
- [ ] Provide Stream[F, Event[...]] instead of a Resource[F, ...] for the subscription.
- [ ] Improve documentation.

## Note

- This project is not affiliated with EventStoreDB. For further information about EventStoreDB, please visit [EventStoreDB](https://eventstore.com/).
- For further information about the Java client, please visit [EventStoreDB Java Client](https://github.com/EventStore/EventStoreDB-Client-Java).
- There's a lot to change/improve, please feel free to open an issue if you have any questions or suggestions, or if you find any bugs.
- For further information on usage and examples, please visit [Dolphin Integration Tests](modules/tests/src/it/scala/).
