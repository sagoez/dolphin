// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.outcome

import cats.Applicative
import cats.syntax.functor.*
import cats.syntax.traverse.*
import com.eventstore.dbclient.Subscription as JSubscription

sealed trait VolatileSubscription extends Product with Serializable {

  def stop: VolatileSubscription = VolatileSubscription.Stop

}

object VolatileSubscription {

  case object Stop  extends VolatileSubscription
  case object Empty extends VolatileSubscription

  private[dolphin] def interpreter[F[_]: Applicative](
    subscription: JSubscription,
    cmds: List[VolatileSubscription]
  ): F[Unit] = {
    def handleCommand(cmd: VolatileSubscription): F[Unit] =
      cmd match {
        case Stop => Applicative[F].pure(subscription.stop())
        case _    => Applicative[F].unit
      }
    cmds.traverse(handleCommand).as(())
  }

}
