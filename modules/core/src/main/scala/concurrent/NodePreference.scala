// Copyright (c) 2022 by Samuel Gomez
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.concurrent

sealed trait NodePreference

object NodePreference {
  case object Leader         extends NodePreference
  case object Follower       extends NodePreference
  case object ReadOnlyLeader extends NodePreference
  case object Any            extends NodePreference

  final implicit class NodePreferenceOps(private val nodePreference: NodePreference) extends AnyVal {

    private[dolphin] def toJava: com.eventstore.dbclient.NodePreference =
      nodePreference match {
        case Leader         => com.eventstore.dbclient.NodePreference.LEADER
        case Follower       => com.eventstore.dbclient.NodePreference.FOLLOWER
        case ReadOnlyLeader => com.eventstore.dbclient.NodePreference.READ_ONLY_REPLICA
        case Any            => com.eventstore.dbclient.NodePreference.RANDOM
      }
  }
}
