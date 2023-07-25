package dolphin

import cats.effect.IO
import cats.effect.kernel.{Ref, Resource}
import cats.syntax.foldable.*
import dolphin.Message.VolatileMessage
import dolphin.suite.ResourceSuite

import weaver.{GlobalRead, IOSuite}
import weaver.scalacheck.Checkers

import java.util.UUID
import scala.concurrent.duration.*

class VolatileSubscriptionListenerSuite(global: GlobalRead) extends IOSuite with ResourceSuite with Checkers {

  override type Res = VolatileSession[IO]

  override def sharedResource: Resource[IO, Res] = global.getOrFailR[Res]()

  test("onEvent with handler should be able to stop appends properly") { session =>
    val uuid = UUID.randomUUID().toString

    def handler(ref: Ref[F, List[String]]): VolatileMessage[IO] => IO[Unit] = {
      case e: Message.Event[IO, VolatileConsumer[IO]]        => ref.update("onEvent" :: _) *> e.consumer.stop
      case _: Message.Confirmation[IO, VolatileConsumer[IO]] => ref.update("onConfirmation" :: _)
      case _: Message.Error[IO, VolatileConsumer[IO]]        => ref.update("onError" :: _)
      case _: Message.Cancelled[IO, VolatileConsumer[IO]]    => ref.update("onCancelled" :: _)
    }

    (for {
      ref   <- Resource.eval(Ref.of(List.empty[String]))
      _     <- session.subscribeToStream(uuid, handler(ref))
      _     <- Resource.eval(session.appendToStream(uuid, "test-event".getBytes, Array.emptyByteArray, "test-data"))
      value <- Resource.eval(ref.get.delayBy(200.millis))
    } yield expect(value.size >= 2) and expect(value.contains("onEvent")) and expect(
      value.contains("onConfirmation")
    ) and expect(
      value.contains("onCancelled")
    )).use(IO.pure)

  }

  test("onEvent with handler should stop when called on any subscription event") { session =>
    val uuid = UUID.randomUUID().toString

    def handler(ref: Ref[F, List[String]]): VolatileMessage[IO] => IO[Unit] = {
      case e: Message.Event[IO, VolatileConsumer[IO]]        => ref.update("onEvent" :: _) *> e.consumer.stop
      case _: Message.Confirmation[IO, VolatileConsumer[IO]] => ref.update("onConfirmation" :: _)
      case _: Message.Error[IO, VolatileConsumer[IO]]        => ref.update("onError" :: _)
      case _: Message.Cancelled[IO, VolatileConsumer[IO]]    => ref.update("onCancelled" :: _)
    }

    (for {
      ref   <- Resource.eval(Ref.of(List.empty[String]))
      _     <- session.subscribeToStream(uuid, handler(ref))
      _     <- Resource.eval(session.appendToStream(uuid, "test-event".getBytes, Array.emptyByteArray, "test-data"))
      // Wait for the subscription to catch up as it is asynchronous
      value <- Resource.eval(ref.get.delayBy(200.millis))
    } yield expect(value.contains("onEvent")) and expect(value.contains("onConfirmation")) and expect(
      value.contains("onCancelled")
    )).use(IO.pure)

  }

  test("onEvent with handler should be called when EventStoreDB appends an event to the subscription".ignore) {
    session =>
      val uuid = UUID.randomUUID().toString

      def appendConcurrently = (1 to 100)
        .toList
        .traverse_(_ => session.appendToStream(uuid, "test-event".getBytes, Array.emptyByteArray, "test-data").start)

      def handler(ref: Ref[F, List[String]]): VolatileMessage[IO] => IO[Unit] = {
        case Message.Event(_, _, _)  => ref.update("onEvent" :: _)
        case Message.Confirmation(_) => ref.update("onConfirmation" :: _)
        case Message.Error(_, _)     => ref.update("onError" :: _)
        case Message.Cancelled(_)    => ref.update("onCancelled" :: _)
      }

      (for {
        ref   <- Resource.eval(Ref.of(List.empty[String]))
        _     <- session.subscribeToStream(uuid, handler(ref))
        _     <- Resource.eval(appendConcurrently)
        value <- Resource.eval(ref.get.delayBy(500.millis))
      } yield expect(expectAtLeastOne(value)("onEvent")) and expect(expectAtLeastN(value, 99)("onEvent")))
        .use(IO.pure)
        .timeout(5.seconds)

  }

}
