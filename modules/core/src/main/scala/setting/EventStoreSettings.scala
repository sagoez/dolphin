// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.setting

import dolphin.concurrent.NodePreference

sealed abstract case class EventStoreSettings private (
  host: String = "localhost",
  port: Int = 2113,
  tls: Boolean = false,
  deadline: Option[Long] = None,
  discoveryInterval: Option[Int] = None,
  gossipTimeout: Option[Int] = None,
  keepAliveTimeout: Option[Long] = None,
  keepAliveInterval: Option[Long] = None,
  maxDiscoverAttempts: Option[Int] = None,
  dnsDiscover: Boolean = false,
  throwOnAppendFailure: Boolean = false,
  nodePreference: Option[NodePreference] = None,
  tlsVerifyCert: Boolean = false
) extends Product
  with Serializable {
  self =>

  private def copy(
    host: String = self.host,
    port: Int = self.port,
    tls: Boolean = self.tls,
    deadline: Option[Long] = self.deadline,
    discoveryInterval: Option[Int] = self.discoveryInterval,
    gossipTimeout: Option[Int] = self.gossipTimeout,
    keepAliveTimeout: Option[Long] = self.keepAliveTimeout,
    keepAliveInterval: Option[Long] = self.keepAliveInterval,
    maxDiscoverAttempts: Option[Int] = self.maxDiscoverAttempts,
    dnsDiscover: Boolean = self.dnsDiscover,
    throwOnAppendFailure: Boolean = self.throwOnAppendFailure,
    nodePreference: Option[NodePreference] = self.nodePreference,
    tlsVerifyCert: Boolean = self.tlsVerifyCert
  ) =
    new EventStoreSettings(
      host,
      port,
      tls,
      deadline,
      discoveryInterval,
      gossipTimeout,
      keepAliveTimeout,
      keepAliveInterval,
      maxDiscoverAttempts,
      dnsDiscover,
      throwOnAppendFailure,
      nodePreference,
      tlsVerifyCert
    ) {}

  /** The host of the EventStoreDB server.
    */
  def withHost(host: String): EventStoreSettings = copy(host = host)

  /** The port of the EventStoreDB server.
    */
  def withPort(port: Int): EventStoreSettings = copy(port = port)

  /** If secure mode is enabled.
    */
  def withTls(tls: Boolean): EventStoreSettings = copy(tls = tls)

  /** An optional length of time (in milliseconds) to use for gRPC deadlines.
    */
  def withDeadline(deadline: Long): EventStoreSettings = copy(deadline = Some(deadline))

  /** How long to wait before retrying a new discovery process (in milliseconds).
    */
  def withDiscoveryInterval(
    discoveryInterval: Int
  ): EventStoreSettings = copy(discoveryInterval = Some(discoveryInterval))

  /** How long to wait for the gossip request to timeout (in seconds).
    */
  def withGossipTimeout(
    gossipTimeout: Int
  ): EventStoreSettings = copy(gossipTimeout = Some(gossipTimeout))

  /** The amount of time (in milliseconds) the sender of the keepalive ping waits for an acknowledgement.
    */
  def withKeepAliveTimeout(
    keepAliveTimeout: Long
  ): EventStoreSettings = copy(keepAliveTimeout = Some(keepAliveTimeout))

  /** The amount of time (in milliseconds) to wait after which a keepalive ping is sent on the transport.
    */
  def withKeepAliveInterval(
    keepAliveInterval: Long
  ): EventStoreSettings = copy(keepAliveInterval = Some(keepAliveInterval))

  /** How many times to attempt connection before throwing.
    */
  def withMaxDiscoverAttempts(
    maxDiscoverAttempts: Int
  ): EventStoreSettings = copy(maxDiscoverAttempts = Some(maxDiscoverAttempts))

  /** If DNS discovery is enabled.
    */
  def withDnsDiscover(dnsDiscover: Boolean): EventStoreSettings = copy(dnsDiscover = dnsDiscover)

  /** If an exception is thrown whether an append operation fails.
    */
  def withThrowOnAppendFailure(
    throwOnAppendFailure: Boolean
  ): EventStoreSettings = copy(throwOnAppendFailure = throwOnAppendFailure)

  /** Preferred node type when picking a node within a cluster.
    */
  def withNodePreference(
    nodePreference: NodePreference
  ): EventStoreSettings = copy(nodePreference = Some(nodePreference))

  /** If secure mode is enabled, is certificate verification enabled.
    */
  def withTlsVerifyCert(
    tlsVerifyCert: Boolean
  ): EventStoreSettings = copy(tlsVerifyCert = tlsVerifyCert)

}

object EventStoreSettings {

  /** Creates a new [[EventStoreSettings]] instance.
    * @return
    *   a new [[EventStoreSettings]] instance.
    */
  val Default: EventStoreSettings = new EventStoreSettings {}

}
