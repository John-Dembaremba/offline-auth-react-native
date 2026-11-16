
package expo.modules.offlineauth

import expo.modules.kotlin.Promise
import expo.modules.kotlin.exception.toCodedException
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import io.github.johndembaremba.offlineauth.OfflineAuthManager
import io.github.johndembaremba.offlineauth.model.CredentialSyncFailureReason
import io.github.johndembaremba.offlineauth.serialization.SnapshotParser

class OfflineAuthModule : Module() {

    override fun definition() = ModuleDefinition {
        Name("OfflineAuth")

        AsyncFunction("getPublicKey") {
            val context = appContext.reactContext
                ?: throw IllegalStateException(
                    "React context is unavailable"
                )

            val manager = OfflineAuthManager.create(context)

            manager.getPublicKey()
        }

        AsyncFunction("sync") { snapshotJson: String, promise: Promise ->
            val context = appContext.reactContext

            if (context == null) {
                promise.reject(
                    IllegalStateException(
                        "React context is unavailable"
                    ).toCodedException()
                )
                return@AsyncFunction
            }

            NativePromiseRunner.launch(
                scope = appContext.backgroundCoroutineScope,
                promise = promise
            ) {
                val snapshot = SnapshotParser().parse(snapshotJson)

                val manager = OfflineAuthManager.create(context)

                val result = manager.sync(snapshot)

                mapOf(
                    "successfulUsernames" to result.successfulUsernames,
                    "failures" to result.failures.map { failure ->
                        val (source, reason) = when (
                            val failureReason = failure.reason
                        ) {
                            is CredentialSyncFailureReason.Local ->
                                "local" to failureReason.reason.code

                            is CredentialSyncFailureReason.Backend ->
                                "backend" to failureReason.reason
                        }

                        mapOf(
                            "username" to failure.username,
                            "source" to source,
                            "reason" to reason,
                            "storedCredentialVersion" to
                                failure.storedCredentialVersion,
                            "incomingCredentialVersion" to
                                failure.incomingCredentialVersion
                        )
                    }
                )
            }
        }

        AsyncFunction("authenticate") {
                username: String,
                password: String,
                promise: Promise ->

            val context = appContext.reactContext

            if (context == null) {
                promise.reject(
                    IllegalStateException(
                        "React context is unavailable"
                    ).toCodedException()
                )
                return@AsyncFunction
            }

            NativePromiseRunner.launch(
                scope = appContext.backgroundCoroutineScope,
                promise = promise
            ) {
                val passwordChars = password.toCharArray()

                try {
                    val manager = OfflineAuthManager.create(context)

                    manager.authenticate(
                        username = username,
                        password = passwordChars
                    )
                } finally {
                    passwordChars.fill('\u0000')
                }
            }
        }
    }
}
