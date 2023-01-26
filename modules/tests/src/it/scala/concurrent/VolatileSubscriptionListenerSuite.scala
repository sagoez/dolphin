// TODO: This test is flaky and needs to be fixed

package dolphin.concurrent.tests

import cats.effect.IO
import cats.effect.kernel.Ref
import cats.effect.kernel.Resource
import cats.syntax.foldable.*
import dolphin.Message.VolatileMessage
import dolphin.{Config, Message, VolatileConsumer, VolatileSession}
import dolphin.tests.ResourceSuite

import java.util.UUID
import scala.concurrent.duration.*

object VolatileSubscriptionListenerSuite extends ResourceSuite {

  override type Res = VolatileSession[IO]

  override def sharedResource: Resource[IO, Res] = VolatileSession.resource(Config.default)

  test("onEvent with handler should be able to stop appends properly") { session =>
    val uuid = UUID.randomUUID().toString

    val ref: Ref[IO, List[String]] = Ref.unsafe(List.empty[String])

    def handler: VolatileMessage[IO] => IO[Unit] = {
      case e: Message.Event[IO, VolatileConsumer[IO]]        => ref.update("onEvent" :: _) *> e.consumer.stop
      case _: Message.Confirmation[IO, VolatileConsumer[IO]] => ref.update("onConfirmation" :: _)
      case _: Message.Error[IO, VolatileConsumer[IO]]        => ref.update("onError" :: _)
      case _: Message.Cancelled[IO, VolatileConsumer[IO]]    => ref.update("onCancelled" :: _)
    }

    (for {
      _     <- session.subscribeToStream(uuid, handler)
      _     <- Resource.eval(session.appendToStream(uuid, "test-event".getBytes, Array.emptyByteArray, "test-data"))
      value <- Resource.eval(ref.get.delayBy(3.second))
    } yield expect(value.size >= 2) and expect(value.contains("onEvent")) and expect(
      value.contains("onConfirmation")
    ) and expect(
      value.contains("onCancelled")
    )).use(IO.pure)

  }

  test("onEvent with handler should stop when called on any subscription event") { session =>
    val uuid = UUID.randomUUID().toString

    val ref: Ref[IO, List[String]] = Ref.unsafe(List.empty[String])

    def handler: VolatileMessage[IO] => IO[Unit] = {
      case e: Message.Event[IO, VolatileConsumer[IO]]        => ref.update("onEvent" :: _) *> e.consumer.stop
      case _: Message.Confirmation[IO, VolatileConsumer[IO]] => ref.update("onConfirmation" :: _)
      case _: Message.Error[IO, VolatileConsumer[IO]]        => ref.update("onError" :: _)
      case _: Message.Cancelled[IO, VolatileConsumer[IO]]    => ref.update("onCancelled" :: _)
    }

    (for {
      _     <- session.subscribeToStream(uuid, handler)
      _     <- Resource.eval(session.appendToStream(uuid, "test-event".getBytes, Array.emptyByteArray, "test-data"))
      // Wait for the subscription to catch up as it is asynchronous
      value <- Resource.eval(ref.get.delayBy(1.second))
    } yield expect(value.contains("onEvent")) and expect(value.contains("onConfirmation")) and expect(
      value.contains("onCancelled")
    )).use(IO.pure)

  }

  // TODO: This test is flaky and needs to be fixed. Dispatcher context dies before the subscription is cancelled
  test("onEvent with handler should be called when EventStoreDB appends an event to the subscription".ignore) {
    session =>
      val uuid = UUID.randomUUID().toString
      val ref  = Ref.unsafe(List.empty[String])

      def appendConcurrently = (1 to 5)
        .toList
        .traverse_(_ => session.appendToStream(uuid, "test-event".getBytes, Array.emptyByteArray, "test-data").start)

      def handler: VolatileMessage[IO] => IO[Unit] = {
        case _: Message.Event[IO, VolatileConsumer[IO]]        => ref.update("onEvent" :: _)
        case _: Message.Confirmation[IO, VolatileConsumer[IO]] => ref.update("onConfirmation" :: _)
        case _: Message.Error[IO, VolatileConsumer[IO]]        => ref.update("onError" :: _)
        case _: Message.Cancelled[IO, VolatileConsumer[IO]]    => ref.update("onCancelled" :: _)
      }

      (for {
        _     <- session.subscribeToStream(uuid, handler)
        _     <- Resource.eval(appendConcurrently)
        // Wait for the subscription to catch up as it is asynchronous
        value <- Resource.eval(ref.get.delayBy(1.seconds))
      } yield expect(value == List("onEvent", "onEvent", "onEvent", "onEvent", "onEvent", "onConfirmation")))
        .use(IO.pure)

  }

}
