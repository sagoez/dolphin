package dolphin.concurrent.tests

import cats.effect.IO
import cats.effect.kernel.Ref
import cats.effect.kernel.Resource
import dolphin.VolatileSession
import dolphin.concurrent.VolatileSubscription
import dolphin.concurrent.VolatileSubscriptionListener
import dolphin.outcome.ResolvedEventOutcome
import dolphin.setting.EventStoreSettings
import dolphin.tests.ResourceSuite

import java.util.UUID
import scala.annotation.unused
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.*

object VolatileSubscriptionListenerSuite extends ResourceSuite {

  override type Res = VolatileSession[IO]

  override def sharedResource: Resource[IO, Res] = VolatileSession.resource(EventStoreSettings.Default)

  test("onEvent with handler should be called when EventStoreDB appends an event to the subscription") { session =>
    val uuid = UUID.randomUUID().toString
    val ref  = Ref.unsafe(List.empty[String])

    def onEvent(
      @unused subscription: VolatileSubscription,
      @unused event: ResolvedEventOutcome[IO]
    ): IO[VolatileSubscription] =
      for {
        _ <- ref.update(_ :+ "onEvent")
      } yield subscription.noop

    def onConfirmation(
      @unused subscription: VolatileSubscription
    ): IO[VolatileSubscription] =
      for {
        _ <- ref.update(_ :+ "onConfirmation")
      } yield subscription.noop

    def onError(
      @unused subscription: VolatileSubscription,
      @unused error: Throwable
    ): IO[VolatileSubscription] =
      for {
        _ <- ref.update(_ :+ "onError")
      } yield subscription.noop

    def onCancelled(
      @unused subscription: VolatileSubscription
    ): IO[VolatileSubscription] =
      for {
        _ <- ref.update(_ :+ "onCancelled")
      } yield subscription.noop

    val listener = VolatileSubscriptionListener.WithHandler[IO](onEvent, onConfirmation, onError, onCancelled)

    for {
      _     <- session.subscribeToStream(uuid, listener)
      _     <- session.appendToStream(uuid, "test-event".getBytes, Array.emptyByteArray, "test-data")
      _     <- session.appendToStream(uuid, "test-event".getBytes, Array.emptyByteArray, "test-data")
      _     <- session.appendToStream(uuid, "test-event".getBytes, Array.emptyByteArray, "test-data")
      // Wait for the subscription to catch up as it is asynchronous
      _     <- IO.sleep(1.second)
      value <- ref.get
    } yield expect(value == List("onConfirmation", "onEvent", "onEvent", "onEvent"))
  }

  test("onEvent with handler should stop when called on any subscription event") { session =>
    val uuid = UUID.randomUUID().toString

    val ref: Ref[IO, List[String]] = Ref.unsafe(List.empty[String])

    def onEvent(
      @unused subscription: VolatileSubscription,
      @unused event: ResolvedEventOutcome[IO]
    ): IO[VolatileSubscription] =
      for {
        _ <- ref.update("onEvent" :: _)
      } yield subscription.stop

    def onConfirmation(
      @unused subscription: VolatileSubscription
    ): IO[VolatileSubscription] =
      for {
        _ <- ref.update("onConfirmation" :: _)
      } yield subscription.noop

    def onError(
      @unused subscription: VolatileSubscription,
      @unused error: Throwable
    ): IO[VolatileSubscription] =
      for {
        _ <- ref.update("onError" :: _)
      } yield subscription.noop

    def onCancelled(
      @unused subscription: VolatileSubscription
    ): IO[VolatileSubscription] =
      for {
        _ <- ref.update("onCancelled" :: _)
      } yield subscription.noop

    val listener = VolatileSubscriptionListener.WithHandler[IO](onEvent, onConfirmation, onError, onCancelled)

    for {
      _     <- session.subscribeToStream(uuid, listener)
      _     <- session.appendToStream(uuid, "test-event".getBytes, Array.emptyByteArray, "test-data")
      // Wait for the subscription to catch up as it is asynchronous
      _     <- IO.sleep(1.second)
      value <- ref.get
    } yield expect(value.contains("onEvent")) and expect(value.contains("onConfirmation")) and expect(
      value.contains("onCancelled")
    )

  }

  test("onEvent with handler should be able to handle concurrent appends properly") { session =>
    val uuid = UUID.randomUUID().toString

    val ref: Ref[IO, List[String]] = Ref.unsafe(List.empty[String])

    def onEvent(
      @unused subscription: VolatileSubscription,
      @unused event: ResolvedEventOutcome[IO]
    ): IO[VolatileSubscription] =
      for {
        _ <- ref.update("onEvent" :: _)
      } yield subscription.noop

    def onConfirmation(
      @unused subscription: VolatileSubscription
    ): IO[VolatileSubscription] =
      for {
        _ <- ref.update("onConfirmation" :: _)
      } yield subscription.noop

    def onError(
      @unused subscription: VolatileSubscription,
      @unused error: Throwable
    ): IO[VolatileSubscription] =
      for {
        _ <- ref.update("onError" :: _)
      } yield subscription.noop

    def onCancelled(
      @unused subscription: VolatileSubscription
    ): IO[VolatileSubscription] =
      for {
        _ <- ref.update("onCancelled" :: _)
      } yield subscription.noop

    val listener = VolatileSubscriptionListener.WithHandler[IO](onEvent, onConfirmation, onError, onCancelled)

    for {
      _     <- session.subscribeToStream(uuid, listener)
      _     <- session.appendToStream(uuid, "test-event".getBytes, Array.emptyByteArray, "test-data").start
      _     <- session.appendToStream(uuid, "test-event".getBytes, Array.emptyByteArray, "test-data").start
      _     <- session.appendToStream(uuid, "test-event".getBytes, Array.emptyByteArray, "test-data").start
      _     <- session
                 .appendToStream(uuid, "test-event".getBytes, Array.emptyByteArray, "test-data")
                 .flatMap(_ => session.appendToStream(uuid, "test-event".getBytes, Array.emptyByteArray, "test-data").start)
      // Wait for the subscription to catch up as it is asynchronous
      _     <- IO.sleep(1.second)
      value <- ref.get
    } yield expect(value == List("onEvent", "onEvent", "onEvent", "onEvent", "onEvent", "onConfirmation"))

  }

  test("onEvent with stream handler should be called when EventStoreDB appends an event to the subscription") {
    session =>
      import fs2.Stream

      val id  = UUID.randomUUID().toString
      val ref = Ref.unsafe(List.empty[String])

      val program =
        (for {
          _ <-
            Stream(UUID.randomUUID())
              .evalMap { uuid =>
                session
                  .appendToStream(
                    id,
                    s"""{"test": "${uuid}"}""".getBytes,
                    Array.emptyByteArray,
                    "test"
                  )
              }
              .meteredStartImmediately(1.seconds)
              .repeatN(4) concurrently session
              .subscribeToStream(id, VolatileSubscriptionListener.WithStreamHandler[IO]())
              .evalTap {
                case Left(error)  => IO(println(error))
                case Right(value) => value.getEventContentType.flatMap(value => ref.update(value :: _))
              }
              .meteredStartImmediately(1.seconds)
              .repeatN(4)

        } yield ())
          .compile
          .drain

      program >> ref
        .get
        .map(value =>
          expect(
            value == List(
              "application/octet-stream",
              "application/octet-stream",
              "application/octet-stream",
              "application/octet-stream"
            )
          )
        )

  }

}
