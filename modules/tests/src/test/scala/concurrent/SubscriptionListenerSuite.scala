// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.concurrent.tests

import dolphin.concurrent.SubscriptionState.Cancelled
import dolphin.concurrent.{PersistentSubscriptionListener, SubscriptionState, VolatileSubscriptionListener}
import dolphin.outcome.ResolvedEventOutcome

import cats.effect.IO
import com.eventstore.dbclient.generator
import weaver.SimpleIOSuite

object SubscriptionListenerSuite extends SimpleIOSuite {

  val volatileStreamListener: VolatileSubscriptionListener.WithStreamHandler[IO] = VolatileSubscriptionListener
    .WithStreamHandler[IO]()

  val persistentSubscriptionListener: PersistentSubscriptionListener.WithStreamHandler[IO] =
    PersistentSubscriptionListener.WithStreamHandler[IO]()

  pureTest("VolatileSubscriptionListener should be able to be created") {
    expect(volatileStreamListener != null)
  }

  pureTest("PersistentSubscriptionListener should be able to be created") {
    expect(persistentSubscriptionListener != null)
  }

  pureTest("VolatileSubscriptionListener listener should be able to be created with an empty state queue") {

    val volatileStreamListener: VolatileSubscriptionListener.WithStreamHandler[IO] = VolatileSubscriptionListener
      .WithStreamHandler[IO]()

    volatileStreamListener.listener.onConfirmation(generator.volatileSubscription)

    expect(volatileStreamListener.queue.size() == 1) and expect(
      volatileStreamListener.queue.peek() == SubscriptionState.Empty
    )
  }

  pureTest("PersistentSubscriptionListener listener should be able to be created with an empty queue") {
    val persistentSubscriptionListener: PersistentSubscriptionListener.WithStreamHandler[IO] =
      PersistentSubscriptionListener.WithStreamHandler[IO]()

    persistentSubscriptionListener.listener.onConfirmation(generator.persistentSubscription)

    expect(persistentSubscriptionListener.queue.size() == 1) and expect(
      persistentSubscriptionListener.queue.peek() == SubscriptionState.Empty
    )
  }

  test(
    "VolatileSubscriptionListener listener should be able to add a Right event to the queue when onEvent is called"
  ) {

    volatileStreamListener.listener.onEvent(generator.volatileSubscription, generator.recordedEvent)

    for {
      one <- ResolvedEventOutcome.make(generator.recordedEvent).getEventData
      two <-
        volatileStreamListener.queue.poll() match {
          case SubscriptionState.Event(event) => event.getEventData
          case _                              => IO(Array.emptyByteArray)
        }
    } yield expect(one sameElements two)

  }

  test(
    "PersistentSubscriptionListener listener should be able to add a Right event to the queue when onEvent is called"
  ) {

    persistentSubscriptionListener
      .listener
      .onEvent(generator.persistentSubscription, generator.retryCount, generator.recordedEvent)

    for {
      one <- ResolvedEventOutcome.make(generator.recordedEvent).getEventData
      two <-
        persistentSubscriptionListener.queue.poll() match {
          case SubscriptionState.Event(event) => event.getEventData
          case _                              => IO(Array.emptyByteArray)
        }
    } yield expect(one sameElements two)
  }

  test(
    "VolatileSubscriptionListener listener should be able to add a Left event from the queue when onError is called"
  ) {

    val volatileStreamListener: VolatileSubscriptionListener.WithStreamHandler[IO] = VolatileSubscriptionListener
      .WithStreamHandler[IO]()

    val exception = new Exception("test exception")

    volatileStreamListener.listener.onError(generator.volatileSubscription, exception)

    for {
      one <- IO(exception)
      two <-
        volatileStreamListener.queue.peek() match {
          case SubscriptionState.Error(error) => IO(error)
          case _                              => IO(new Exception("non matching exception"))
        }
    } yield expect(one == two)
  }

  test(
    "PersistentSubscriptionListener listener should be able to add a Left event from the queue when onError is called"
  ) {

    val persistentSubscriptionListener: PersistentSubscriptionListener.WithStreamHandler[IO] =
      PersistentSubscriptionListener.WithStreamHandler[IO]()

    val exception = new Exception("test exception")

    persistentSubscriptionListener.listener.onError(generator.persistentSubscription, exception)

    for {
      one <- IO(exception)
      two <-
        persistentSubscriptionListener.queue.poll() match {
          case SubscriptionState.Error(error) => IO(error)
          case _                              => IO(new Exception("non matching exception"))
        }
    } yield expect(one == two)
  }

  pureTest(
    "VolatileSubscriptionListener listener should be able to cancel queue sync when onCancelled is called"
  ) {

    volatileStreamListener.listener.onCancelled(generator.volatileSubscription)

    expect(volatileStreamListener.queue.contains(Cancelled))
  }

  pureTest(
    "PersistentSubscriptionListener listener should be able to cancel queue sync when onCancelled is called"
  ) {

    persistentSubscriptionListener.listener.onCancelled(generator.persistentSubscription)

    expect(persistentSubscriptionListener.queue.contains(Cancelled))
  }

}
