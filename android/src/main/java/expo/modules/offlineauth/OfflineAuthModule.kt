
package expo.modules.offlineauth

import expo.modules.kotlin.Promise
import expo.modules.kotlin.exception.toCodedException
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import io.github.johndembaremba.offlineauth.OfflineAuthManager
import io.github.johndembaremba.offlineauth.model.CredentialSyncFailureReason
import io.github.johndembaremba.offlineauth.serialization.SnapshotParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

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
            val scope = appContext.backgroundCoroutineScope
            val context = appContext.reactContext

            if (context == null) {
                promise.reject(
                    IllegalStateException(
                        "React context is unavailable"
                    ).toCodedException()
                )
                return@AsyncFunction
            }

            scope.launch {
                try {
                    val snapshot = SnapshotParser().parse(snapshotJson)
                    val manager = OfflineAuthManager.create(context)

                    val result = manager.sync(snapshot)

                    val response = mapOf(
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

                    promise.resolve(response)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    promise.reject(e.toCodedException())
                }
            }
        }
    }
}
