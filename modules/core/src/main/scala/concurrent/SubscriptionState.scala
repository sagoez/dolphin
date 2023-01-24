// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.concurrent

import java.util.concurrent.ConcurrentLinkedQueue

sealed trait SubscriptionState[+A] extends Product with Serializable { self =>

  def map[B](f: A => B): SubscriptionState[B] =
    self match {
      case SubscriptionState.Event(event) => SubscriptionState.Event(f(event))
      case SubscriptionState.Empty        => SubscriptionState.Empty
      case SubscriptionState.Cancelled    => SubscriptionState.Cancelled
      case SubscriptionState.Error(error) => SubscriptionState.Error(error)
    }

  def flatMap[B](f: A => SubscriptionState[B]): SubscriptionState[B] =
    self match {
      case SubscriptionState.Event(event) => f(event)
      case SubscriptionState.Empty        => SubscriptionState.Empty
      case SubscriptionState.Cancelled    => SubscriptionState.Cancelled
      case SubscriptionState.Error(error) => SubscriptionState.Error(error)
    }

  def fold[B](empty: => B, cancelled: => B, error: Throwable => B, event: A => B): B =
    self match {
      case SubscriptionState.Empty       => empty
      case SubscriptionState.Cancelled   => cancelled
      case SubscriptionState.Error(err)  => error(err)
      case SubscriptionState.Event(evnt) => event(evnt)
    }

  def filter(f: A => Boolean): SubscriptionState[A] =
    self match {
      case SubscriptionState.Event(event) if f(event) => self
      case _                                          => SubscriptionState.Empty
    }

  def filterNot(f: A => Boolean): SubscriptionState[A] =
    self match {
      case SubscriptionState.Event(event) if !f(event) => self
      case _                                           => SubscriptionState.Empty
    }

  def collect[B](pf: PartialFunction[A, B]): SubscriptionState[B] =
    self match {
      case SubscriptionState.Event(event) if pf.isDefinedAt(event) => SubscriptionState.Event(pf(event))
      case _                                                       => SubscriptionState.Empty
    }

  def get: Option[A] =
    self match {
      case SubscriptionState.Event(event) => Some(event)
      case _                              => None
    }

  def getOrElse[B >: A](default: => B): B =
    self match {
      case SubscriptionState.Event(event) => event
      case _                              => default
    }

  def isEmpty: Boolean =
    self match {
      case SubscriptionState.Empty => true
      case _                       => false
    }

  def isDefined: Boolean =
    self match {
      case SubscriptionState.Empty => false
      case _                       => true
    }

  def isCancelled: Boolean =
    self match {
      case SubscriptionState.Cancelled => true
      case _                           => false
    }

  def isError: Boolean =
    self match {
      case SubscriptionState.Error(_) => true
      case _                          => false
    }

  def isEvent: Boolean =
    self match {
      case SubscriptionState.Event(_) => true
      case _                          => false
    }
}

object SubscriptionState {

  def concurrentLinkedQueue[A]: ConcurrentLinkedQueue[SubscriptionState[A]] =
    new ConcurrentLinkedQueue[SubscriptionState[A]]()

  case object Empty extends SubscriptionState[Nothing]

  case object Cancelled extends SubscriptionState[Nothing]

  final case class Error(error: Throwable) extends SubscriptionState[Nothing]

  final case class Event[A](event: A) extends SubscriptionState[A]

}
