// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.concurrent.tests

import dolphin.concurrent.{PersistentSubscriptionListener, VolatileSubscriptionListener}
import dolphin.outcome.ResolvedEventOutcome

import cats.effect.IO
import com.eventstore.dbclient.generator
import weaver.FunSuite

object SubscriptionListenerSuite extends FunSuite {

  val volatileStreamListener: VolatileSubscriptionListener.WithStreamHandler[IO] = VolatileSubscriptionListener
    .WithStreamHandler[IO]()

  val persistentSubscriptionListener: PersistentSubscriptionListener.WithStreamHandler[IO] =
    PersistentSubscriptionListener.WithStreamHandler[IO]()

  test("VolatileSubscriptionListener should be able to be created") {
    expect(volatileStreamListener != null)
  }

  test("PersistentSubscriptionListener should be able to be created") {
    expect(persistentSubscriptionListener != null)
  }

  test("VolatileSubscriptionListener listener should be able to be created with an empty queue") {

    expect(volatileStreamListener.queue.isEmpty && volatileStreamListener.queue.size() == 0)
  }

  test("PersistentSubscriptionListener listener should be able to be created with an empty queue") {

    expect(persistentSubscriptionListener.queue.isEmpty && persistentSubscriptionListener.queue.size() == 0)
  }

  test(
    "VolatileSubscriptionListener listener should be able to add a Right event to the queue when onEvent is called"
  ) {

    import cats.effect.unsafe.implicits.global

    volatileStreamListener.listener.onEvent(generator.volatileSubscription, generator.recordedEvent)

    expect(
      volatileStreamListener
        .queue
        .size() == 1 && volatileStreamListener.queue.peek().map(_.getEventData.unsafeRunSync()) == Right(
        ResolvedEventOutcome.make(generator.recordedEvent).getEventData.unsafeRunSync()
      )
    )
  }

  test(
    "PersistentSubscriptionListener listener should be able to add a Right event to the queue when onEvent is called"
  ) {

    import cats.effect.unsafe.implicits.global

    persistentSubscriptionListener
      .listener
      .onEvent(generator.persistentSubscription, generator.retryCount, generator.recordedEvent)

    expect(
      persistentSubscriptionListener
        .queue
        .size() == 1 && persistentSubscriptionListener.queue.peek().map(_.getEventData.unsafeRunSync()) == Right(
        ResolvedEventOutcome.make(generator.recordedEvent).getEventData.unsafeRunSync()
      )
    )
  }

  test(
    "VolatileSubscriptionListener listener should be able to add a Left event from the queue when onError is called"
  ) {

    import cats.effect.unsafe.implicits.global

    val exception = new Exception("test exception")

    volatileStreamListener.listener.onError(generator.volatileSubscription, exception)

    expect(
      volatileStreamListener.queue.size() == 2 && volatileStreamListener
        .queue
        .poll()
        .map(_.getEventData.map(new String(_)).unsafeRunSync())
        == Right(
          """{"test": "test"}"""
        ) && volatileStreamListener.queue.peek() == Left(exception)
    )
  }

  test(
    "PersistentSubscriptionListener listener should be able to add a Left event from the queue when onError is called"
  ) {

    import cats.effect.unsafe.implicits.global

    val exception = new Exception("test exception")

    persistentSubscriptionListener.listener.onError(generator.persistentSubscription, exception)

    expect(
      persistentSubscriptionListener.queue.size() == 2 && persistentSubscriptionListener
        .queue
        .poll()
        .map(_.getEventData.map(new String(_)).unsafeRunSync())
        == Right(
          """{"test": "test"}"""
        ) && persistentSubscriptionListener.queue.peek() == Left(exception)
    )
  }

  test(
    "VolatileSubscriptionListener listener should be able to remove all events from the queue when onCancelled is called"
  ) {

    volatileStreamListener.listener.onCancelled(generator.volatileSubscription)

    expect(volatileStreamListener.queue.size() == 0 && volatileStreamListener.queue.isEmpty)
  }

  test(
    "PersistentSubscriptionListener listener should be able to remove all events from the queue when onCancelled is called"
  ) {

    persistentSubscriptionListener.listener.onCancelled(generator.persistentSubscription)

    expect(persistentSubscriptionListener.queue.size() == 0 && persistentSubscriptionListener.queue.isEmpty)
  }

}
