// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package com.eventstore.dbclient

import java.util.UUID
import java.util.concurrent.CompletableFuture

import com.eventstore.dbclient.*
import com.eventstore.dbclient.proto.persistentsubscriptions.Persistent
import com.eventstore.dbclient.proto.streams.StreamsOuterClass
import io.grpc.stub.ClientCallStreamObserver

object generator {
  val uuid: UUID                = java.util.UUID.randomUUID()
  val eventData: Array[Byte]    = """{"test": "test"}""".getBytes()
  val userMetadata: Array[Byte] = Array.emptyByteArray
  val position: Position        = new Position(0L, 0L)

  val javaMap = new java.util.HashMap[String, String]()

  javaMap.put("content-type", "application/json")
  javaMap.put("event-type", "event-type")
  javaMap.put("is-json", "true")
  javaMap.put("type", "event-type")
  javaMap.put("created", 1670964607154L.toString)

  val recordedEvent: ResolvedEvent =
    new ResolvedEvent(
      new com.eventstore.dbclient.RecordedEvent(
        "test-stream-id",
        0L,
        uuid,
        position,
        javaMap,
        eventData,
        userMetadata
      ),
      new com.eventstore.dbclient.RecordedEvent(
        "test-stream-id",
        0L,
        uuid,
        position,
        javaMap,
        eventData,
        userMetadata
      ),
      position
    )

  val checkpointer: Checkpointer =
    new Checkpointer {

      override def onCheckpoint(subscription: Subscription, position: Position): CompletableFuture[Void] = ???

    }

  val volatileObserver: ClientCallStreamObserver[StreamsOuterClass.ReadReq] =
    new ClientCallStreamObserver[StreamsOuterClass.ReadReq] {

      def onNext(value: StreamsOuterClass.ReadReq): Unit = ???

      def cancel(message: String, cause: Throwable): Unit = ???

      def disableAutoInboundFlowControl(): Unit = ???

      def onCompleted(): Unit = ???

      def isReady: Boolean = ???

      def onError(x: Throwable): Unit = ???

      def request(count: Int): Unit = ???

      def setMessageCompression(x: Boolean): Unit = ???

      def setOnReadyHandler(onReadyHandler: Runnable): Unit = ???
    }

  val persistentObserver: ClientCallStreamObserver[Persistent.ReadReq] =
    new ClientCallStreamObserver[Persistent.ReadReq] {

      def onNext(value: Persistent.ReadReq): Unit = ???

      def cancel(message: String, cause: Throwable): Unit = ???

      def disableAutoInboundFlowControl(): Unit = ???

      def onCompleted(): Unit = ???

      def isReady: Boolean = ???

      def onError(x: Throwable): Unit = ???

      def request(count: Int): Unit = ???

      def setMessageCompression(x: Boolean): Unit = ???

      def setOnReadyHandler(onReadyHandler: Runnable): Unit = ???
    }

  val volatileSubscription =
    new Subscription(
      volatileObserver,
      "test-stream-id",
      checkpointer
    )

  val persistentSubscription =
    new PersistentSubscription(
      persistentObserver,
      "test-stream-id"
    )

  val retryCount: Int = 10

}
