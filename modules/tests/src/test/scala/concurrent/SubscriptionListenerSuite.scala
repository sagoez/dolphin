// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.concurrent

import cats.effect.IO
import weaver.FunSuite

import com.eventstore.dbclient.generator

object SubscriptionListenerSuite extends FunSuite {

  test("SubscriptionListener should be able to be created") {
    val listener = SubscriptionListener.default[IO]

    expect(listener.underlying != null)
  }

  test("SubscriptionListener underlying should be able to be created with an empty queue") {
    val listener = SubscriptionListener.default[IO]

    expect(listener.underlying.queue.isEmpty && listener.underlying.queue.size() == 0)
  }

  test("SubscriptionListener underlying should be able to add a Right event to the queue when onEvent is called") {
    val listener = SubscriptionListener.default[IO]

    listener.underlying.onEvent(generator.subscription, generator.recordedEvent)

    expect(
      listener.underlying.queue.size() == 1 && listener.underlying.queue.peek() == Right(
        generator.recordedEvent.getOriginalEvent.getEventData
      )
    )
  }

  test("SubscriptionListener underlying should be able to add a Left event from the queue when onError is called") {
    val listener  = SubscriptionListener.default[IO]
    val exception = new Exception("test exception")

    listener.underlying.onError(generator.subscription, exception)

    expect(listener.underlying.queue.size() == 1 && listener.underlying.queue.peek() == Left(exception))
  }

  test(
    "SubscriptionListener underlying should be able to remove all events from the queue when onCancelled is called"
  ) {
    val listener = SubscriptionListener.default[IO]

    listener.underlying.onCancelled(generator.subscription)

    expect(listener.underlying.queue.size() == 0 && listener.underlying.queue.isEmpty)
  }

}
