package dolphin.concurrent.tests

import cats.effect.{IO, Resource}
import cats.effect.kernel.Ref
import dolphin.Message.PersistentMessage
import dolphin.{Config, Message, PersistentSession}
import dolphin.setting.PersistentSubscriptionSettings
import dolphin.tests.ResourceSuite
import fs2.Stream

import java.util.UUID
import scala.concurrent.duration.DurationInt

object PersistentSubscriptionListenerSuite extends ResourceSuite {

  def sharedResource: Resource[IO, Res] = PersistentSession.resource(Config.default)

  override type Res = PersistentSession[IO]

  // TODO: This test is not working and needs to be fixed, it hangs every time
  test("should be able to stop subscription".ignore) { session =>
    val uuid = UUID.randomUUID().toString

    val ref = Ref.unsafe(List.empty[String])

    val handler: PersistentMessage[IO] => IO[Unit] = {
      case Message.Event(_, event, _) => logger.info(s"Received event: $event")
      case Message.Error(_, error)    => logger.error(error)(s"Received error: $error")
      case Message.Cancelled(_)       => ref.update("OnCancelled" :: _)
      case Message.Confirmation(sus)  => ref.update("OnConfirmed" :: _) >> sus.stop
    }

    (for {
      _       <- Stream.eval(session.createToStream(uuid, uuid).attempt)
      _       <- session.subscribeToStream(uuid, uuid, PersistentSubscriptionSettings.Default, handler)
      content <- Stream.eval(
                   ref
                     .get
                     .map { list =>
                       list.contains("OnConfirmed") && list.contains("OnCancelled")
                     }
                     .delayBy(1.seconds)
                 )
    } yield expect(content)).compile.lastOrError
  }

}
