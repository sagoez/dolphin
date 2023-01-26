// TODO: This test is flaky and needs to be fixed

//package dolphin.concurrent.tests
//
//import cats.effect.{IO, Resource}
//import cats.effect.kernel.Ref
//import cats.syntax.traverse.*
//import dolphin.Message.PersistentMessage
//import dolphin.{Config, Message, PersistentSession}
//import dolphin.setting.{PersistentSubscriptionSettings, UpdatePersistentSubscriptionToAllSettings}
//import dolphin.tests.ResourceSuite
//import io.grpc.StatusRuntimeException
//import fs2.Stream
//
//import java.util.UUID
//import scala.concurrent.duration.DurationInt
//
//object PersistentSubscriptionListenerSuite extends ResourceSuite {
//
//  def sharedResource: Resource[IO, Res] = PersistentSession.resource(Config.default)
//
//  override type Res = PersistentSession[IO]
//
//  test("should be able to create a persistent subscription to the all stream") { session =>
//    val uuid = UUID.randomUUID().toString
//    for {
//      _           <- session.createToAll(uuid)
//      status      <- session.listAll.flatMap(_.map(_.getStatus).sequence)
//      groupName   <- session.listAll.flatMap(_.map(_.getGroupName).sequence)
//      eventSource <- session.listAll.flatMap(_.map(_.getEventSource).sequence)
//    } yield expect(status.contains("Live")) and expect(groupName.contains(uuid)) and expect(
//      eventSource.contains("$all")
//    )
//  }
//
//  test("should fail with \"StatusRuntimeException\" if Subscription group test on stream $all exists.") { session =>
//    val uuid = UUID.randomUUID().toString
//
//    for {
//      _   <- session.createToAll(uuid)
//      res <- shouldFailWith(session.createToAll(uuid), classOf[StatusRuntimeException])
//    } yield expect(res)
//  }
//
//  test("should be able to create a persistent subscription to the stream") { session =>
//    val uuid = UUID.randomUUID().toString
//    for {
//      _           <- session.createToStream(uuid, uuid)
//      status      <- session.listToStream(uuid).flatMap(_.map(_.information.getStatus).sequence)
//      groupName   <- session.listToStream(uuid).flatMap(_.map(_.information.getGroupName).sequence)
//      eventSource <- session.listToStream(uuid).flatMap(_.map(_.information.getEventSource).sequence)
//    } yield expect(status.contains("Live")) and expect(groupName.contains(uuid)) and expect(
//      eventSource.contains(uuid)
//    )
//  }
//
//  test("should fail with \"StatusRuntimeException\" if Subscription group test on stream test exists.") { session =>
//    val uuid = UUID.randomUUID().toString
//
//    for {
//      _   <- session.createToStream(uuid, uuid)
//      res <- shouldFailWith(session.createToStream(uuid, uuid), classOf[StatusRuntimeException])
//    } yield expect(res)
//  }
//
//  test("should delete a persistent subscription to the all stream") { session =>
//    val uuid = UUID.randomUUID().toString
//    for {
//      _         <- session.createToAll(uuid)
//      _         <- session.deleteToAll(uuid)
//      groupName <- session.listAll.flatMap(_.map(_.getGroupName).sequence)
//    } yield expect(!groupName.contains(uuid))
//  }
//
//  test("should delete a persistent subscription to the stream") { session =>
//    val uuid = UUID.randomUUID().toString
//    for {
//      _         <- session.createToStream(uuid, uuid)
//      _         <- session.deleteToStream(uuid, uuid)
//      groupName <- session.listToStream(uuid).flatMap(_.map(_.information.getGroupName).sequence)
//    } yield expect(!groupName.contains(uuid))
//  }
//
//  test("should be able to get information about a persistent subscription to the all stream") { session =>
//    val uuid = UUID.randomUUID().toString
//    for {
//      _      <- session.createToAll(uuid)
//      info   <- session.getInfoToAll(uuid)
//      status <-
//        info.map(_.information.getGroupName) match {
//          case Some(value) => value.map(value => expect(value == uuid))
//          case None        => IO.pure(failure("No information found"))
//        }
//    } yield status
//  }
//
//  test("should be able to get information about a persistent subscription to the stream") { session =>
//    val uuid = UUID.randomUUID().toString
//    for {
//      _      <- session.createToStream(uuid, uuid)
//      info   <- session.getInfoToStream(uuid, uuid)
//      status <-
//        info.map(_.information.getGroupName) match {
//          case Some(value) => value.map(value => expect(value == uuid))
//          case None        => IO.pure(failure("No information found"))
//        }
//    } yield status
//  }
//
//  test("should be able to update a persistent subscription to the all stream") { session =>
//    val uuid = UUID.randomUUID().toString
//    for {
//      _      <- session.createToAll(uuid)
//      _      <- session.updateToAll(uuid, UpdatePersistentSubscriptionToAllSettings.Default.withHistoryBufferSize(400))
//      info   <- session.getInfoToAll(uuid)
//      status <-
//        info.map(_.settings.getHistoryBufferSize) match {
//          case Some(value) => value.map(value => expect(value == 400))
//          case None        => IO.pure(failure("Settings do not match"))
//        }
//    } yield status
//  }
//
//  test("should be able to stop subscription") { session =>
//    val uuid = UUID.randomUUID().toString
//
//    val ref = Ref.unsafe(List.empty[String])
//
//    val handler: PersistentMessage[IO] => IO[Unit] = {
//      case Message.Event(_, event, _) => logger.info(s"Received event: $event")
//      case Message.Error(_, error)    => logger.error(error)(s"Received error: $error")
//      case Message.Cancelled(_)       => ref.update("OnCancelled" :: _)
//      case Message.Confirmation(sus)  => ref.update("OnConfirmed" :: _) >> sus.stop
//    }
//
//    (for {
//      _       <- Stream.eval(session.createToStream(uuid, uuid).attempt)
//      _       <- session.subscribeToStream(uuid, uuid, PersistentSubscriptionSettings.Default, handler)
//      content <- Stream.eval(
//                   ref
//                     .get
//                     .map { list =>
//                       list.contains("OnConfirmed") && list.contains("OnCancelled")
//                     }
//                     .delayBy(1.seconds)
//                 )
//    } yield expect(content)).compile.lastOrError
//  }
//
//}
