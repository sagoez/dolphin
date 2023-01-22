// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.concurrent.tests

import dolphin.concurrent.VolatileSubscriptionListener
import dolphin.outcome.ResolvedEventOutcome

import cats.effect.IO
import com.eventstore.dbclient.generator
import weaver.FunSuite

object SubscriptionListenerSuite extends FunSuite {

  val streamListener: VolatileSubscriptionListener.WithStreamHandler[IO] = VolatileSubscriptionListener
    .WithStreamHandler()

  test("SubscriptionListener should be able to be created") {
    expect(streamListener != null)
  }

  test("SubscriptionListener listener should be able to be created with an empty queue") {

    expect(streamListener.queue.isEmpty && streamListener.queue.size() == 0)
  }

  test("SubscriptionListener listener should be able to add a Right event to the queue when onEvent is called") {

    import cats.effect.unsafe.implicits.global

    streamListener.listener.onEvent(generator.subscription, generator.recordedEvent)

    expect(
      streamListener.queue.size() == 1 && streamListener.queue.peek().map(_.getEventData.unsafeRunSync()) == Right(
        ResolvedEventOutcome.make(generator.recordedEvent).getEventData.unsafeRunSync()
      )
    )
  }

  test("SubscriptionListener listener should be able to add a Left event from the queue when onError is called") {

    import cats.effect.unsafe.implicits.global

    val exception = new Exception("test exception")

    streamListener.listener.onError(generator.subscription, exception)

    expect(
      streamListener.queue.size() == 2 && streamListener
        .queue
        .poll()
        .map(_.getEventData.map(new String(_)).unsafeRunSync())
        == Right(
          """{"test": "test"}"""
        ) && streamListener.queue.peek() == Left(exception)
    )
  }

  test(
    "SubscriptionListener listener should be able to remove all events from the queue when onCancelled is called"
  ) {

    streamListener.listener.onCancelled(generator.subscription)

    expect(streamListener.queue.size() == 0 && streamListener.queue.isEmpty)
  }

}
