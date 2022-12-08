// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.circe.domain

import io.circe.Error

sealed trait ReadDecodeResult[+A] extends Product with Serializable { self =>

  /** Transforms the value of a successful result using the given function.
    *
    * @param f
    *   the function to apply
    * @return
    *   the result of the function
    */
  def map[B](f: A => B): ReadDecodeResult[B] =
    self match {
      case ReadDecodeResult.Success(value)  => ReadDecodeResult.Success(f(value))
      case ReadDecodeResult.Failure(errors) => ReadDecodeResult.Failure(errors)
    }

  /** Will perform the given function ```success``` if the result is a success, otherwise will perform the given
    * function ```failure```.
    *
    * @param failure
    *   the function to apply if the result is a failure
    * @param success
    *   the function to apply if the result is a success
    * @return
    */
  def fold[B](failure: List[Error] => B, success: A => B): B =
    self match {
      case ReadDecodeResult.Success(value)  => success(value)
      case ReadDecodeResult.Failure(errors) => failure(errors)
    }

  /** Returns the value if the result is a success, otherwise returns None.
    */
  def get: Option[A] =
    self match {
      case ReadDecodeResult.Success(value) => Some(value)
      case ReadDecodeResult.Failure(_)     => None
    }

  /** Returns the value if the result is a success, otherwise returns the given default value.
    *
    * @param default
    *   the default value to return if the result is a failure
    */
  def getOrElse[B >: A](default: => B): B = get.getOrElse(default)

  /** If the result is a failure, it i'll return that instead
    *
    * @param that
    *   the result to return if the result is a failure
    */
  def orElse[B >: A](that: => ReadDecodeResult[B]): ReadDecodeResult[B] =
    self match {
      case ReadDecodeResult.Success(value) => ReadDecodeResult.Success(value)
      case ReadDecodeResult.Failure(_)     => that
    }
}

object ReadDecodeResult {
  case class Success[A](value: A)         extends ReadDecodeResult[A]
  case class Failure(errors: List[Error]) extends ReadDecodeResult[Nothing]
}
