// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin

import dolphin.Event as DEvent

import cats.{Applicative, Monad}

sealed trait Message[F[_], T <: Consumer[F]] extends Product with Serializable { self =>

  def consumer: T

  def get: Option[DEvent] =
    self match {
      case Message.Event(_, event, _) => Some(event)
      case _                          => None
    }

  def getOrThrow: DEvent =
    self match {
      case Message.Event(_, event, _) => event
      case _                          => throw new IllegalStateException("Message is not an event")
    }

  def isEvent: Boolean =
    self match {
      case _: Message.Event[F, T] => true
      case _                      => false
    }

  def isError: Boolean =
    self match {
      case _: Message.Error[F, T] => true
      case _                      => false
    }

  def isCancelled: Boolean =
    self match {
      case _: Message.Cancelled[F, T] => true
      case _                          => false
    }

  def isConfirmation: Boolean =
    self match {
      case _: Message.Confirmation[F, T] => true
      case _                             => false
    }

  def fold[A](onEvent: DEvent => A, onError: Throwable => A, onCancel: A, onConfirmation: A): A =
    self match {
      case Message.Event(_, event, _)    => onEvent(event)
      case Message.Error(_, error)       => onError(error)
      case _: Message.Cancelled[F, T]    => onCancel
      case _: Message.Confirmation[F, T] => onConfirmation
    }

  def flatTap(f: Message[F, T] => F[Unit])(implicit F: Monad[F]): F[Message[F, T]] =
    F.flatMap(f(self))(_ => F.pure(self))

  def map[A](f: Message[F, T] => A)(implicit F: Applicative[F]): F[A] = F.pure(f(self))

}

object Message {

  type VolatileMessage[F[_]]   = Message[F, VolatileConsumer[F]]
  type PersistentMessage[F[_]] = Message[F, PersistentConsumer[F]]

  final case class Event[F[_], T <: Consumer[F]](
    consumer: T,
    event: DEvent,
    retryCount: Option[Int] = None
  ) extends Message[F, T]

  final case class Error[F[_], T <: Consumer[F]](
    consumer: T,
    error: Throwable
  ) extends Message[F, T]

  final case class Cancelled[F[_], T <: Consumer[F]](
    consumer: T
  ) extends Message[F, T]

  final case class Confirmation[F[_], T <: Consumer[F]](
    consumer: T
  ) extends Message[F, T]
}
