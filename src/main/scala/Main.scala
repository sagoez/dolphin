import com.eventstore.dbclient.*
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import io.circe.syntax.EncoderOps

import java.util.UUID
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.util.Try

sealed trait Result[+A] { self =>

  import Result.*

  def map[B](f: A => B): Result[B] =
    self match {
      case Success(a, warnings) => Success(f(a), warnings)
      case Failure(e)           => Failure(e)
    }

  def flatMap[B](f: A => Result[B]): Result[B] =
    self match {
      case Success(value, warnings) =>
        f(value) match {
          case Success(value, warnings2) => Success(value, warnings ++ warnings2)
          case Failure(errors)           => Failure(errors)
        }

      case Failure(errors) => Failure(errors)
    }

}

object Main extends App {

  type Client = EventConnection

  sealed trait EventInteraction { self =>

    import EventInteraction.*

    def ++(that: EventInteraction): EventInteraction =
      (self, that) match {
        case (FromRead(a), FromRead(b))                       => FromRead(a ++ b)
        case (FromRead(a), FromWrite(b))                      => FromReadAndWrite(a, b)
        case (FromWrite(a), FromRead(b))                      => FromReadAndWrite(b, a)
        case (FromWrite(a), FromWrite(b))                     => FromWrite(a ++ b)
        case (FromReadAndWrite(a, b), FromRead(c))            => FromReadAndWrite(a ++ c, b)
        case (FromRead(a), FromReadAndWrite(b, c))            => FromReadAndWrite(a ++ b, c)
        case (FromReadAndWrite(a, b), FromWrite(c))           => FromReadAndWrite(a, b ++ c)
        case (FromWrite(a), FromReadAndWrite(b, c))           => FromReadAndWrite(b, a ++ c)
        case (FromReadAndWrite(a, b), FromReadAndWrite(c, d)) => FromReadAndWrite(a ++ c, b ++ d)
      }
  }

  sealed abstract class Action(val client: Client = EventConnection.empty) extends Serializable {
    self =>

    import Action.*

    def map[B](f: Action => B): B = f(self)

    def flatMap(f: Action => Action): Action = f(self)

    def orElse(that: Action): Action = OrElse(self, that)

    def read(
      stream: String,
      options: ReadStreamOptions = ReadStreamOptions.get(),
    ): Action = self + Read(client, stream, options)

    private def +(action: Action): Action =
      self match {
        case read: Read                           => read
        case write: Write                         => write
        case orElse: OrElse                       => orElse
        case Sequence(actions) if actions.isEmpty => action
        case Sequence(actions)                    => Sequence(actions :+ action)
        case _                                    => Sequence(List(action))
      }

    def write(
      stream: String,
      events: List[String],
      options: AppendToStreamOptions = AppendToStreamOptions.get(),
    ): Action = self + Write(client, stream, events, options)

  }

  final case class EventConnection(connection: () => Option[EventStoreDBClient])

  final case class TestEvent(id: String, name: String)

  implicit class ActionOps(action: Action) {

    import Action.*
    import Result.*

    def execute: Result[EventInteraction] =
      action match {
        case Read(client, stream, options) =>
          client.connection() match {
            case Some(value) =>
              Try(value.readStream(stream, options).get()) match {
                case util.Failure(exception) => Failure(List(exception.getMessage))
                case util.Success(value)     => Success(EventInteraction.FromRead(value.getEvents.asScala.toList))
              }
            case None        => Failure(List("Unable to connect to EventStore, please check your connection string"))
          }
        case OrElse(lhs, rhs)              =>
          lhs.execute match {
            case Success(value, warnings) => Success(value, warnings)
            case Failure(errors)          =>
              rhs.execute match {
                case Success(value, warnings) => Success(value, warnings)
                case Failure(errors2)         => Failure(errors ++ errors2)
              }
          }

        case Write(client, stream, events, options) =>
          client.connection() match {
            case Some(value) =>
              events.map { event =>
                val eventId   = UUID.randomUUID()
                val `type`    = "event"
                val eventData = EventData
                  .builderAsJson(`type`, event)
                  .eventId(eventId)
                  .build()
                Try(value.appendToStream(stream, options, eventData).get())
              } match {
                case Nil  => Failure(List("No events to write"))
                case list =>
                  val (successes, failures) = list.partition(_.isSuccess)
                  if (failures.isEmpty) {
                    Success(EventInteraction.FromWrite(successes.map(_.get)), List.empty)
                  } else {
                    Failure(failures.map(_.failed.get.getMessage))
                  }
              }
            case None        => Failure(List("Unable to connect to EventStore, please check your connection string"))
          }
        // TODO: Log warnings
        case Sequence(actions)                      =>
          // Return a list of results, if any of the results are a failure, return the failure
          actions.map(_.execute) match {
            case ::(head, next) =>
              next.foldLeft(head) {
                case (Success(value, warnings), Success(value2, warnings2)) =>
                  Success(value ++ value2, warnings ++ warnings2)
                case (Failure(errors), Success(_, _))                       => Failure(errors)
                case (Success(_, _), Failure(errors))                       => Failure(errors)
                case (Failure(errors), Failure(errors2))                    => Failure(errors ++ errors2)
              }
            case Nil            => Failure(List("Please provide at least one action"))
          }

        case action => Failure(List(s"Unknown action $action"))
      }

  }

  object EventConnection {
    def empty: EventConnection = EventConnection(() => None)
  }

  object EventInteraction {
    final case class FromRead(value: List[ResolvedEvent]) extends EventInteraction

    final case class FromWrite(value: List[WriteResult]) extends EventInteraction

    final case class FromReadAndWrite(read: List[ResolvedEvent], write: List[WriteResult]) extends EventInteraction

  }

  object Action {

    def fromUri(value: String): Action =
      new Action(
        EventConnection(() => Some(EventStoreDBClient.create(EventStoreDBConnectionString.parse(value))))
      ) {}

    final case class Read(override val client: Client, stream: String, options: ReadStreamOptions) extends Action

    final case class Write(
      override val client: Client,
      stream: String,
      events: List[String],
      options: AppendToStreamOptions,
    ) extends Action

    final case class OrElse(lhs: Action, rhs: Action) extends Action

    final case class Sequence(actions: List[Action])
      extends Action(actions.map(_.client).headOption.getOrElse(EventConnection.empty))

    //    object Sequence {
    //      def apply(actions: Sequence): Sequence = Sequence(actions.actions)
    //
    //      def apply(actions: Action*): Sequence = Sequence(actions.flatMap {
    //        case actions: Action.Sequence => actions.actions
    //        case action                   => List(action)
    //      }.toList)
    //
    //    }

  }

  object TestEvent {

    def randomListJson(size: Int): List[String] = randomList(size).map(_.asJson.noSpaces)

    def randomList(size: Int): List[TestEvent] = List.fill(size)(random)

    def random: TestEvent = TestEvent(UUID.randomUUID().toString, UUID.randomUUID().toString)

    implicit val codec: Codec[TestEvent] = deriveCodec
  }

  Action
    .fromUri("esdb://localhost:2113?tls=false")
    .write("PimpMyStream", TestEvent.randomListJson(10))
    .read("PimpMyStream")
    .execute
    .map(value => println(value))

}

object Result {

  final case class Success[+A](value: A, warnings: List[String] = Nil) extends Result[A]

  final case class Failure(errors: List[String]) extends Result[Nothing]
}
