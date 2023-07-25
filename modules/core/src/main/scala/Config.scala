// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin

import scala.annotation.implicitNotFound
import scala.concurrent.duration.Duration

import dolphin.Config.Base.*
import dolphin.Config.*
import dolphin.concurrent.NodePreference



sealed abstract class Config {
  val host: EventStoreHost
  val port: EventStorePort
  val tls: EventStoreTls
  val deadline: Option[Long]                 = None
  val discoveryInterval: Option[Int]         = None
  val gossipTimeout: Option[Int]             = None
  val keepAliveTimeout: Option[Long]         = None
  val keepAliveInterval: Option[Long]        = None
  val maxDiscoverAttempts: Option[Int]       = None
  val dnsDiscover: Boolean                   = false
  val throwOnAppendFailure: Boolean          = false
  val nodePreference: Option[NodePreference] = None
  val tlsVerifyCert: Boolean                 = false
}

object Config {

  case class EventStoreHost(host: String)
  case class EventStorePort(port: Int)
  case class EventStoreTls(tls: Boolean)

  sealed trait Base

  object Base {

    sealed trait Empty    extends Base
    sealed trait Optional extends Base

    sealed trait Host extends Base
    sealed trait Port extends Base
    sealed trait Tls  extends Base

    type Required = Empty & Host & Port & Tls
  }

  case class ConfigBuilder[B <: Base] protected (
    _host: EventStoreHost = EventStoreHost(""),
    _port: EventStorePort = EventStorePort(0),
    _tls: EventStoreTls = EventStoreTls(false),
    _deadline: Option[Long] = Some(Deadline),
    _discoveryInterval: Option[Int] = Some(DiscoveryInterval),
    _gossipTimeout: Option[Int] = Some(GossipTimeout),
    _keepAliveTimeout: Option[Long] = Some(KeepAliveTimeout),
    _keepAliveInterval: Option[Long] = Some(KeepAliveInterval),
    _maxDiscoverAttempts: Option[Int] = Some(MaxDiscoverAttempts),
    _dnsDiscover: Boolean = false,
    _throwOnAppendFailure: Boolean = false,
    _nodePreference: Option[NodePreference] = Some(NodePreference.Leader),
    _tlsVerifyCert: Boolean = false
  ) { self =>

    /** The host of the EventStoreDB server.
      */
    def withHost(host: EventStoreHost): ConfigBuilder[B & Host] = self.copy(_host = host)

    def withHost(host: String): ConfigBuilder[B & Host] = withHost(EventStoreHost(host))

    /** The port of the EventStoreDB server.
      */
    def withPort(port: EventStorePort): ConfigBuilder[B & Port] = self.copy(_port = port)

    /** The port of the EventStoreDB server.
      */
    def withPort(port: Int): ConfigBuilder[B & Port] = withPort(EventStorePort(port))

    /** If secure mode is enabled.
      */
    def withTls(tls: EventStoreTls): ConfigBuilder[B & Tls] = self.copy(_tls = tls)

    /** If secure mode is enabled.
      */
    def withTls(tls: Boolean): ConfigBuilder[B & Tls] = withTls(EventStoreTls(tls))

    /** An optional length of time to use for gRPC deadlines.
      */
    def withDeadline(deadline: Duration): ConfigBuilder[B & Optional] = self.copy(_deadline = Some(deadline.toMillis))

    /** How long to wait before retrying a new discovery process.
      */
    def withDiscoveryInterval(
      discoveryInterval: Duration
    ): ConfigBuilder[B & Optional] = self.copy(_discoveryInterval = Some(discoveryInterval.toMillis.toInt))

    /** How long to wait for the gossip request to timeout.
      */
    def withGossipTimeout(
      gossipTimeout: Duration
    ): ConfigBuilder[B & Optional] = self.copy(_gossipTimeout = Some(gossipTimeout.toMillis.toInt))

    /** The amount of time the sender of the keepalive ping waits for an acknowledgement.
      */
    def withKeepAliveTimeout(
      keepAliveTimeout: Duration
    ): ConfigBuilder[B & Optional] = self.copy(_keepAliveTimeout = Some(keepAliveTimeout.toMillis))

    /** The amount of time to wait after which a keepalive ping is sent on the transport.
      */
    def withKeepAliveInterval(
      keepAliveInterval: Duration
    ): ConfigBuilder[B & Optional] = self.copy(_keepAliveInterval = Some(keepAliveInterval.toMillis))

    /** How many times to attempt connection before throwing.
      */
    def withMaxDiscoverAttempts(
      maxDiscoverAttempts: Int
    ): ConfigBuilder[B & Optional] = self.copy(_maxDiscoverAttempts = Some(maxDiscoverAttempts))

    /** If DNS discovery is enabled.
      */
    def withDnsDiscover(dnsDiscover: Boolean): ConfigBuilder[B & Optional] = self.copy(_dnsDiscover = dnsDiscover)

    /** If an exception is thrown whether an append operation fails.
      */
    def withThrowOnAppendFailure(
      throwOnAppendFailure: Boolean
    ): ConfigBuilder[B & Optional] = self.copy(_throwOnAppendFailure = throwOnAppendFailure)

    /** Preferred node type when picking a node within a cluster.
      */
    def withNodePreference(
      nodePreference: NodePreference
    ): ConfigBuilder[B & Optional] = self.copy(_nodePreference = Some(nodePreference))

    /** If secure mode is enabled, is certificate verification enabled.
      */
    def withTlsVerifyCert(
      tlsVerifyCert: Boolean
    ): ConfigBuilder[B & Optional] = self.copy(_tlsVerifyCert = tlsVerifyCert)

    def build(
      implicit @implicitNotFound(
        "You must specify a host, port and tls. Try using Config.Default instead."
      ) ev: B <:< Base.Required
    ): Config =
      new Config {
        val host: EventStoreHost                               = _host
        val port: EventStorePort                               = _port
        val tls: EventStoreTls                                 = _tls
        override val deadline: Option[CommitUnsigned]          = _deadline
        override val discoveryInterval: Option[Int]            = _discoveryInterval
        override val gossipTimeout: Option[Int]                = _gossipTimeout
        override val keepAliveTimeout: Option[CommitUnsigned]  = _keepAliveTimeout
        override val keepAliveInterval: Option[CommitUnsigned] = _keepAliveInterval
        override val maxDiscoverAttempts: Option[Int]          = _maxDiscoverAttempts
        override val dnsDiscover: Boolean                      = _dnsDiscover
        override val throwOnAppendFailure: Boolean             = _throwOnAppendFailure
        override val nodePreference: Option[NodePreference]    = _nodePreference
        override val tlsVerifyCert: Boolean                    = _tlsVerifyCert
      }

  }

  /** Creates a new [[Config]] instance.
    *
    * @return
    *   a new [[Config]] instance.
    */
  val Default: Config =
    Config
      .Builder
      .withHost("localhost")
      .withPort(2113)
      .withTls(false)
      .build
  object Builder extends ConfigBuilder[Base.Empty]()
}
