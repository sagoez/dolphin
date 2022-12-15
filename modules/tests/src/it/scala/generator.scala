package dolphin.tests

import org.scalacheck.Gen

import java.util.UUID

object generator {
  def idGen[A](fa: UUID => A): Gen[A]   = Gen.uuid.map(fa)
  def nesGen[A](f: String => A): Gen[A] = nonEmptyStringGen.map(f)

  val nonEmptyStringGen: Gen[String] = Gen
    .chooseNum(21, 40)
    .flatMap { n =>
      Gen.buildableOfN[String, Char](n, Gen.alphaChar)
    }
}
