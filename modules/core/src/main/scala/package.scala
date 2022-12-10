// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package object dolphin {
  type Event    = Array[Byte]
  type Metadata = Array[Byte]

  /** The type that symbolizes an event with its metadata where _1 is the event and _2 is the metadata
    */
  type EventWithMetadata = (Event, Option[Metadata])

}
