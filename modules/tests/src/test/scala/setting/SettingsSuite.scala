// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.setting.tests

import dolphin.setting.{
  CreatePersistentSubscriptionToAllSettings as PS,
  CreatePersistentSubscriptionToStreamSettings as PSS,
  *
}

import com.eventstore.dbclient.*
import weaver.FunSuite

object SettingsSuite extends FunSuite {

  test("AppendToStreamSettings should return Java WriteStreamOptions") {
    val options = AppendToStreamOptions.get().getClass
    expect(AppendToStreamSettings.Default.getClass == options)
  }

  test("CreatePersistentSubscriptionToAllSettings should return java CreatePersistentSubscriptionToAllOptions") {
    val options = CreatePersistentSubscriptionToAllOptions.get().getClass
    expect(PS.Default.getClass == options)
  }

  test("CreatePersistentSubscriptionToStreamSettings should return java CreatePersistentSubscriptionToAllOptions") {
    val options = CreatePersistentSubscriptionToStreamOptions.get().getClass
    expect(PSS.Default.getClass == options)
  }

  test("DeletePersistentSubscriptionSettings should return java DeletePersistentSubscriptionOptions") {
    val options = DeletePersistentSubscriptionOptions.get().getClass
    expect(DeletePersistentSubscriptionSettings.Default.getClass == options)
  }

  test("DeleteStreamSettings should return Java DeleteStreamOptions") {
    val options = DeleteStreamOptions.get().getClass
    expect(DeleteStreamSettings.Default.getClass == options)
  }

  test("EventStoreSettings should return default settings") {
    val options = EventStoreSettings.Default
    expect(!options.tls) and expect(options.deadline.isEmpty) and expect(options.tlsVerifyCert) and expect(
      !options.dnsDiscover
    ) and expect(options.discoveryInterval.isEmpty) and expect(options.gossipTimeout.isEmpty) and expect(
      options.keepAliveInterval.isEmpty
    ) and expect(options.keepAliveTimeout.isEmpty) and expect(options.maxDiscoverAttempts.isEmpty) and expect(
      !options.throwOnAppendFailure
    ) and expect(options.nodePreference.isEmpty)
  }

  test("GetPersistentSubscriptionInfoSettings should return java GetPersistentSubscriptionOptions") {
    val options = GetPersistentSubscriptionInfoOptions.get().getClass
    expect(GetPersistentSubscriptionInfoSettings.Default.getClass == options)
  }

  test("ListPersistentSubscriptionSettings should return java ListPersistentSubscriptionOptions") {
    val options = ListPersistentSubscriptionsOptions.get().getClass
    expect(ListPersistentSubscriptionsSettings.Default.getClass == options)
  }

  test("ReadStreamSettings should return Java ReadStreamOptions") {
    val options = ReadStreamOptions.get().getClass
    expect(ReadStreamSettings.Default.getClass == options)
  }

  test("ReplayParkedMessagesSettings should return java ReplayParkedMessagesOptions") {
    val options = ReplayParkedMessagesOptions.get().getClass
    expect(ReplayParkedMessagesSettings.Default.getClass == options)
  }

  test("RestartPersistentSubscriptionSettings should return java RestartPersistentSubscriptionOptions") {
    val options = RestartPersistentSubscriptionSubsystemOptions.get().getClass
    expect(RestartPersistentSubscriptionSubsystemSettings.Default.getClass == options)
  }

  test("SubscriptionFilterSettings should return java SubscriptionFilter") {
    val options =
      SubscriptionFilter
        .newBuilder()
        .withEventTypeRegularExpression("^eventType-194$")
        .build()
        .getClass

    expect(
      SubscriptionFilterSettings
        .Default
        .withEventTypePrefix("^eventType-194$")
        .withStreamNamePrefix("^eventType-194$")
        .build
        .getClass == options
    )
  }

  test("SubscriptionToStreamSettings should return Java SubscribeToStreamOptions") {
    val options = SubscribeToStreamOptions.get().getClass
    expect(SubscriptionToStreamSettings.Default.getClass == options)
  }

  test("UpdatePersistentSubscriptionToAllSettings should return java UpdatePersistentSubscriptionToAllOptions") {
    val options = UpdatePersistentSubscriptionToAllOptions.get().getClass
    expect(UpdatePersistentSubscriptionToAllSettings.Default.getClass == options)
  }

  test("UpdatePersistentSubscriptionToStreamSettings should return java UpdatePersistentSubscriptionToStreamOptions") {
    val options = UpdatePersistentSubscriptionToStreamOptions.get().getClass
    expect(UpdatePersistentSubscriptionToStreamSettings.Default.getClass == options)
  }
}
