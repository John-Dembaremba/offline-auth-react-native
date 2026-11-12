package expo.modules.offlineauth

import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import io.github.johndembaremba.offlineauth.OfflineAuthManager

class OfflineAuthModule : Module() {

    override fun definition() = ModuleDefinition {
        Name("OfflineAuth")

        AsyncFunction("getPublicKey") {
            val context = appContext.reactContext
                ?: throw IllegalStateException("React context is unavailable")

            val manager = OfflineAuthManager.create(context)

            manager.getPublicKey()
        }
    }
}
