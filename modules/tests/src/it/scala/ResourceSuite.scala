package dolphin.tests

import cats.effect.IO
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.noop.NoOpLogger
import weaver.IOSuite
import weaver.scalacheck.{CheckConfig, Checkers}

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

}
