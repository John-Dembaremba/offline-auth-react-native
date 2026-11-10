package expo.modules.offlineauth

import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition

class OfflineAuthModule : Module() {
  override fun definition() = ModuleDefinition {
    Name("OfflineAuth")

    AsyncFunction("setValueAsync") { value: String ->
    }
  }
}
