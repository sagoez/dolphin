// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin

import dolphin.Event as DEvent

sealed trait Message[F[_], T <: Consumer[F]] extends Product with Serializable { self =>
  def consumer: T

  def get: Option[DEvent[F]] =
    self match {
      case Message.Event(_, event, _) => Some(event)
      case _                          => None
    }

  def getOrThrow: DEvent[F] =
    self match {
      case Message.Event(_, event, _) => event
      case Message.Error(_, error)    => throw error
      case Message.Cancelled(_)       => throw new IllegalStateException("Message cancelled")
      case Message.Confirmation(_)    => throw new IllegalStateException("Message confirmed")
    }

  def isEvent: Boolean =
    self match {
      case Message.Event(_, _, _) => true
      case _                      => false
    }

  def isError: Boolean =
    self match {
      case Message.Error(_, _) => true
      case _                   => false
    }

  def isCancelled: Boolean =
    self match {
      case Message.Cancelled(_) => true
      case _                    => false
    }

  def isConfirmation: Boolean =
    self match {
      case Message.Confirmation(_) => true
      case _                       => false
    }

  def fold[A](onEvent: DEvent[F] => A, onError: Throwable => A, onCancel: A, onConfirmation: A): A =
    self match {
      case Message.Event(_, event, _) => onEvent(event)
      case Message.Error(_, error)    => onError(error)
      case Message.Cancelled(_)       => onCancel
      case Message.Confirmation(_)    => onConfirmation
    }

}

object Message {

  type VolatileMessage[F[_]]   = Message[F, VolatileConsumer[F]]
  type PersistentMessage[F[_]] = Message[F, PersistentConsumer[F]]

  final case class Event[F[_], T <: Consumer[F]](
    consumer: T,
    event: DEvent[F],
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
