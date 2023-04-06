package dolphin

import cats.effect.kernel.Ref
import cats.effect.{IO, Resource}
import dolphin.Message.PersistentMessage
import dolphin.setting.PersistentSubscriptionSettings
import fs2.Stream
import dolphin.suite.ResourceSuite

import java.util.UUID
import scala.concurrent.duration.DurationInt

object PersistentSubscriptionListenerSuite extends ResourceSuite {

  def sharedResource: Resource[IO, Res] = PersistentSession.resource(Config.Default)

  override type Res = PersistentSession[IO]

  test("should be able to stop subscription with resource type subscription") { session =>
    val uuid = UUID.randomUUID().toString

    def handler(ref: Ref[F, List[String]]): MessageHandler[F, PersistentMessage[F]] = {
      case Message.Event(_, event, _) => logger.info(s"Received event: $event")
      case Message.Error(_, error)    => logger.error(error)(s"Received error: $error")
      case Message.Cancelled(_)       => ref.update("OnCancelled" :: _)
      case Message.Confirmation(sus)  => ref.update("OnConfirmed" :: _) >> sus.stop
    }

    (for {
      ref     <- Resource.eval(Ref.of(List.empty[String]))
      _       <- Resource.eval(session.createToStream(uuid, uuid))
      _       <- session.subscribeToStream(uuid, uuid, PersistentSubscriptionSettings.Default, handler(ref))
      content <- Resource.eval(
                   ref
                     .get
                     .map { list =>
                       list.contains("OnConfirmed") && list.contains("OnCancelled")
                     }
                     .delayBy(100.millis)
                 )
    } yield expect(content)).use(IO.pure)

  }

  // TODO: Fix this test is flaky
  test("should be able to stop subscription with stream type subscription") { session =>
    val uuid = UUID.randomUUID().toString

    (for {
      ref         <- Stream.eval(Ref.of(List.empty[String]))
      _           <- Stream.eval(session.createToStream(uuid, uuid))
      _           <- session.subscribeToStream(uuid, uuid, PersistentSubscriptionSettings.Default).take(2).evalMap {
                       case Message.Event(_, event, _) => IO.println(s"Received event: $event")
                       case Message.Error(_, error)    => IO.println(s"Received error: $error")
                       case Message.Cancelled(_)       => ref.update("OnCancelled" :: _)
                       case Message.Confirmation(sus)  => ref.update("OnConfirmed" :: _) >> sus.stop
                     }
      expectation <- Stream.eval(
                       ref
                         .get
                         .map { list =>
                           list.contains("OnConfirmed") && list.contains("OnCancelled")
                         }
                     )
    } yield expect(expectation)).compile.lastOrError.timeout(5.second)

  }

}
