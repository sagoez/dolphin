package dolphin.tests

import cats.data.Validated
import cats.effect.IO
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.noop.NoOpLogger
import weaver.{Expectations, IOSuite, TestName}
import weaver.scalacheck.{CheckConfig, Checkers}
import scala.concurrent.duration.*

abstract class ResourceSuite extends IOSuite with Checkers {

  implicit val logger: SelfAwareStructuredLogger[IO] = NoOpLogger[IO]

  override def checkConfig: CheckConfig = CheckConfig.default.copy(minimumSuccessful = 1)

  def expectAtLeastOne[A, B](value: List[A])(equalTo: B): Boolean = value.contains(equalTo)

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

  def testWithRetry(name: TestName, retries: Int = 3): PartiallyAppliedTest = {
    var count                        = 0
    var delay                        = 100.milliseconds
    var result: PartiallyAppliedTest = null
    while (count < retries)
      try {
        result = super.test(name)
        count = retries
      } catch {
        case _: Throwable =>
          count += 1
          if (count < retries) {
            println(s"Test failed, retrying in ${delay.toMillis} ms ($count/$retries)...")
            Thread.sleep(delay.toMillis)
            delay = delay * 2
          } else {
            println("Maximum number of retries reached")
          }
      }
    result
  }

}
