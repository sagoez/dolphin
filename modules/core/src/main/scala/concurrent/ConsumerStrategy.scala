// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.concurrent

sealed trait ConsumerStrategy

object ConsumerStrategy {
  case object DispatchToSingle extends ConsumerStrategy
  case object RoundRobin       extends ConsumerStrategy
  case object Pinned           extends ConsumerStrategy

  private[dolphin] implicit class ConsumerStrategyOps(val self: ConsumerStrategy) extends AnyVal {

    def toJava: com.eventstore.dbclient.NamedConsumerStrategy =
      self match {
        case DispatchToSingle => com.eventstore.dbclient.NamedConsumerStrategy.DISPATCH_TO_SINGLE
        case RoundRobin       => com.eventstore.dbclient.NamedConsumerStrategy.ROUND_ROBIN
        case Pinned           => com.eventstore.dbclient.NamedConsumerStrategy.PINNED
      }
  }

  private[dolphin] implicit class NamedConsumerStrategyOps(val strategy: com.eventstore.dbclient.NamedConsumerStrategy)
    extends AnyVal {

    def fromJava: ConsumerStrategy =
      if (strategy.isPinned)
        Pinned
      else if (strategy.isRoundRobin)
        RoundRobin
      else
        DispatchToSingle
  }

}
