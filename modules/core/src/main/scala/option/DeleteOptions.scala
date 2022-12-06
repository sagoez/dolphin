// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.option

import scala.util.Try

import com.eventstore.dbclient.DeleteStreamOptions

sealed abstract case class DeleteOptions private () extends Product with Serializable {
  self =>

  /** Returns options with default values.
    */
  def get: Try[DeleteStreamOptions] = Try(DeleteStreamOptions.get())

}

// Adding this for sake of completeness, but it's not needed
object DeleteOptions {

  def default: DeleteOptions = new DeleteOptions {}

}
