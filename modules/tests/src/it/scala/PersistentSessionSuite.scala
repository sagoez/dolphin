package dolphin.tests

import dolphin.PersistentSession
import dolphin.setting.EventStoreSettings

import cats.effect.IO
import cats.effect.kernel.Resource

object PersistentSessionSuite extends ResourceSuite {

  override type Res = PersistentSession[IO]

  override def sharedResource: Resource[IO, Res] = PersistentSession.resource(EventStoreSettings.Default)

  // TODO: Add tests
}
