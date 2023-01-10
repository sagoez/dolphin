// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.concurrent

import com.eventstore.dbclient

sealed trait NackAction

object NackAction {
  case object Park extends NackAction

  case object Retry extends NackAction

  case object Skip extends NackAction

  case object Stop extends NackAction

  case object Unknown extends NackAction

  final implicit class NackActionOps(private val action: NackAction) extends AnyVal {

    def toJava: dbclient.NackAction =
      action match {
        case Park  => dbclient.NackAction.Park
        case Retry => dbclient.NackAction.Retry
        case Skip  => dbclient.NackAction.Skip
        case Stop  => dbclient.NackAction.Stop
        case _     => dbclient.NackAction.Unknown
      }

  }

  final implicit class JavaNackActionOps(private val action: dbclient.NackAction) extends AnyVal {

    def toScala: NackAction =
      action match {
        case dbclient.NackAction.Park  => Park
        case dbclient.NackAction.Retry => Retry
        case dbclient.NackAction.Skip  => Skip
        case dbclient.NackAction.Stop  => Stop
        case _                         => Unknown
      }

  }
}
