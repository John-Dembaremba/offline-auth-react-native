
package expo.modules.offlineauth

import expo.modules.kotlin.Promise
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class NativePromiseRunnerTest {

    private class RecordingPromise : Promise {

        private val completion = CountDownLatch(1)

        val resolutions = AtomicInteger(0)
        val rejections = AtomicInteger(0)

        @Volatile
        var resolvedValue: Any? = null
            private set

        @Volatile
        var rejectedCode: String? = null
            private set

        @Volatile
        var rejectedMessage: String? = null
            private set

        override fun resolve(value: Any?) {
            resolvedValue = value
            resolutions.incrementAndGet()
            completion.countDown()
        }

        override fun reject(
            code: String?,
            message: String?,
            cause: Throwable?
        ) {
            rejectedCode = code
            rejectedMessage = message
            rejections.incrementAndGet()
            completion.countDown()
        }

        fun awaitCompletion(): Boolean =
            completion.await(5, TimeUnit.SECONDS)

        fun assertSettledOnce() {
            assertEquals(
                1,
                resolutions.get() + rejections.get()
            )
        }
    }

    private fun createScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Test
    fun successfulOperationResolvesOnce() {
        val scope = createScope()
        val promise = RecordingPromise()

        try {
            NativePromiseRunner.launch(
                scope = scope,
                promise = promise
            ) {
                "success"
            }

            assertTrue(promise.awaitCompletion())
            assertEquals("success", promise.resolvedValue)
            assertEquals(1, promise.resolutions.get())
            assertEquals(0, promise.rejections.get())

            promise.assertSettledOnce()
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun failedOperationRejectsOnce() {
        val scope = createScope()
        val promise = RecordingPromise()

        try {
            NativePromiseRunner.launch(
                scope = scope,
                promise = promise
            ) {
                throw IllegalStateException("Test failure")
            }

            assertTrue(promise.awaitCompletion())
            assertEquals(0, promise.resolutions.get())
            assertEquals(1, promise.rejections.get())
            assertNotNull(promise.rejectedCode)

            promise.assertSettledOnce()
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun cancellationBeforeExecutionRejectsOnce() {
        val scope = createScope()
        val promise = RecordingPromise()
        val executed = AtomicInteger(0)

        scope.cancel()

        NativePromiseRunner.launch(
            scope = scope,
            promise = promise
        ) {
            executed.incrementAndGet()
            "should-not-execute"
        }

        assertTrue(promise.awaitCompletion())
        assertEquals(0, executed.get())
        assertEquals(0, promise.resolutions.get())
        assertEquals(1, promise.rejections.get())
        assertEquals(
            "ERR_OFFLINE_AUTH_CANCELLED",
            promise.rejectedCode
        )

        promise.assertSettledOnce()
    }

    @Test
    fun cancellationDuringExecutionRejectsOnce() = runBlocking {
        val scope = createScope()
        val promise = RecordingPromise()
        val started = CompletableDeferred<Unit>()

        try {
            NativePromiseRunner.launch(
                scope = scope,
                promise = promise
            ) {
                started.complete(Unit)
                awaitCancellation()
            }

            withTimeout(5000) {
                started.await()
            }

            scope.cancel()

            assertTrue(promise.awaitCompletion())
            assertEquals(0, promise.resolutions.get())
            assertEquals(1, promise.rejections.get())
            assertEquals(
                "ERR_OFFLINE_AUTH_CANCELLED",
                promise.rejectedCode
            )

            promise.assertSettledOnce()
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun completionAndCancellationSettleOnlyOnce() = runBlocking {
        val scope = createScope()
        val promise = RecordingPromise()
        val started = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()

        try {
            NativePromiseRunner.launch(
                scope = scope,
                promise = promise
            ) {
                started.complete(Unit)
                release.await()
                "completed"
            }

            withTimeout(5000) {
                started.await()
            }

            release.complete(Unit)
            scope.cancel()

            assertTrue(promise.awaitCompletion())

            val totalSettlements =
                promise.resolutions.get() +
                    promise.rejections.get()

            assertEquals(1, totalSettlements)
            assertFalse(
                promise.resolutions.get() > 0 &&
                    promise.rejections.get() > 0
            )

            promise.assertSettledOnce()
        } finally {
            scope.cancel()
        }
    }
}
