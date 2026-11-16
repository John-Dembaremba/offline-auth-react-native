
package expo.modules.offlineauth

import expo.modules.kotlin.Promise
import expo.modules.kotlin.exception.toCodedException
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal object NativePromiseRunner {

    fun launch(
        scope: CoroutineScope,
        promise: Promise,
        operation: suspend () -> Any?
    ) {
        val settled = AtomicBoolean(false)

        val job = scope.launch {
            try {
                val result = operation()

                if (settled.compareAndSet(false, true)) {
                    promise.resolve(result)
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                if (settled.compareAndSet(false, true)) {
                    promise.reject(exception.toCodedException())
                }
            }
        }

        job.invokeOnCompletion { cause ->
            if (
                cause != null &&
                settled.compareAndSet(false, true)
            ) {
                if (cause is CancellationException) {
                    promise.reject(
                        "ERR_OFFLINE_AUTH_CANCELLED",
                        "Offline authentication operation was cancelled",
                        cause
                    )
                } else {
                    promise.reject(
                        "ERR_OFFLINE_AUTH_NATIVE",
                        "Offline authentication operation failed",
                        cause
                    )
                }
            }
        }
    }
}
