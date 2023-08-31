package dolphin.suite

import cats.data.Validated
import cats.effect.IO
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.noop.NoOpLogger
import weaver.{Expectations, GlobalResource}

import scala.concurrent.duration.*
import cats.effect.Resource
import weaver.GlobalWrite
import dolphin.PersistentSession
import dolphin.Config
import weaver.ResourceTag
import scala.concurrent.duration.Duration
import dolphin.VolatileSession

trait ResourceSuite {

  implicit val PersistentResourceTag: ResourceTag[PersistentSession[IO]] = ResourceTag
    .classBasedInstance[PersistentSession[IO]]
  implicit val VolatileResourceTag: ResourceTag[VolatileSession[IO]]     = ResourceTag
    .classBasedInstance[VolatileSession[IO]]

  implicit val logger: SelfAwareStructuredLogger[IO] = NoOpLogger[IO]

  def expectAtLeastOne[A, B <: A](value: List[A])(equalTo: B): Boolean = value.contains[A](equalTo)

  def expectAtLeastN[A, B](value: List[A], n: Int)(equalTo: B): Boolean = value.count(value => value == equalTo) >= n

  def expectAtMostN[A, B](value: List[A], n: Int)(equalTo: B): Boolean = value.count(value => value == equalTo) <= n

  def expectExactlyN[A, B](value: List[A], n: Int)(equalTo: B): Boolean = value.count(value => value == equalTo) == n

  def shouldFailWith[A](
    io: IO[A],
    exception: Class[? <: Throwable]
  ): IO[Boolean] = io.attempt.map(_.fold(_.getClass == exception, _ => false))

  def withRetry(
    io: IO[Expectations],
    maxRetries: Int = 10,
    maxDuration: Duration = 10.seconds
  ): IO[Expectations] = {
    val maxDurationMillis = maxDuration.toMillis
    val maxRetriesCapped  = maxRetries.min(10)
    val maxDurationCapped = maxDurationMillis.min(10000L)

    def retry(numberOfRetries: Int, delay: Long): IO[Expectations] = io.flatMap { value =>
      value.run match {
        case Validated.Valid(_)   => IO.pure(value)
        case Validated.Invalid(e) =>
          if (numberOfRetries < maxRetriesCapped && delay < maxDurationCapped) {
            IO.sleep(delay.millis) *> retry(numberOfRetries + 1, delay * 2)
          } else {
            IO.raiseError(new Exception(e.toList.mkString(", ")))
          }
      }
    }

    retry(0, 100)
  }

}

object SharedResourceSuite extends ResourceSuite with GlobalResource {

  override def sharedResources(global: GlobalWrite): Resource[IO, Unit] =
    for {
      persistentSession <- PersistentSession.resource[IO](
                             Config
                               .Builder
                               .withHost("localhost")
                               .withPort(2113)
                               .withTls(false)
                               .build
                           )
      volatileSession   <- VolatileSession.resource[IO](Config.Default)
      _                 <- global.putR(persistentSession)
      _                 <- global.putR(volatileSession)
    } yield ()

}
