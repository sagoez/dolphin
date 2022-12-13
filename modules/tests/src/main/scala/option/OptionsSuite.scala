// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.option

import weaver.FunSuite
import com.eventstore.dbclient.{AppendToStreamOptions, DeleteStreamOptions, ReadStreamOptions, SubscribeToStreamOptions}

import scala.util.Try

object OptionsSuite extends FunSuite {
  test("Delete options should return Java DeleteStreamOptions") {
    val options = Try(DeleteStreamOptions.get()).map(_.getClass)
    expect(DeleteOptions.default.get.map(_.getClass) == options)
  }

  test("Read options should return Java ReadStreamOptions") {
    val options = Try(ReadStreamOptions.get()).map(_.getClass)
    expect(ReadOptions.default.get.map(_.getClass) == options)

  }

  test("Write options should return Java WriteStreamOptions") {
    val options = Try(AppendToStreamOptions.get()).map(_.getClass)
    expect(WriteOptions.default.get.map(_.getClass) == options)
  }

  test("Subscribe options should return Java SubscribeToStreamOptions") {
    val options = Try(SubscribeToStreamOptions.get()).map(_.getClass)
    expect(SubscriptionOptions.default.get.map(_.getClass) == options)
  }
}
