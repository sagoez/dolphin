// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.concurrent.tests

import dolphin.concurrent.SubscriptionListener

import cats.effect.IO
import com.eventstore.dbclient.generator
import weaver.FunSuite

object SubscriptionListenerSuite extends FunSuite {

  val streamListener: SubscriptionListener.WithStream[IO] = SubscriptionListener.WithStream()

  test("SubscriptionListener should be able to be created") {
    expect(streamListener != null)
  }

  test("SubscriptionListener underlying should be able to be created with an empty queue") {

    expect(streamListener.queue.isEmpty && streamListener.queue.size() == 0)
  }

  test("SubscriptionListener underlying should be able to add a Right event to the queue when onEvent is called") {

    streamListener.java.onEvent(generator.subscription, generator.recordedEvent)

    expect(
      streamListener.queue.size() == 1 && streamListener.queue.peek() == Right(
        generator.recordedEvent.getOriginalEvent.getEventData
      )
    )
  }

  test("SubscriptionListener underlying should be able to add a Left event from the queue when onError is called") {
    val exception = new Exception("test exception")

    streamListener.java.onError(generator.subscription, exception)

    expect(
      streamListener.queue.size() == 2 && streamListener.queue.poll().map(value => new String(value)) == Right(
        """{"test": "test"}"""
      ) && streamListener.queue.peek() == Left(exception)
    )
  }

  test(
    "SubscriptionListener underlying should be able to remove all events from the queue when onCancelled is called"
  ) {

    streamListener.java.onCancelled(generator.subscription)

    expect(streamListener.queue.size() == 0 && streamListener.queue.isEmpty)
  }

}
