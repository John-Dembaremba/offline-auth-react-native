
package expo.modules.offlineauth

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.johndembaremba.offlineauth.OfflineAuthManager
import io.github.johndembaremba.offlineauth.model.CredentialSyncFailureReason
import io.github.johndembaremba.offlineauth.serialization.SnapshotParser
import io.github.johndembaremba.offlineauth.sync.UnsupportedProtocolVersionException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OfflineAuthIntegrationTest {

    private val context
        get() = InstrumentationRegistry
            .getInstrumentation()
            .targetContext

    @Test
    fun backendFailureIsPreservedInSyncResult() = runBlocking {
        val snapshotJson = """
            {
                "protocol_version": 1,
                "credentials": [],
                "failures": [
                    {
                        "user_id": "2",
                        "username": "sync_test_bob",
                        "reason": "unusable_password"
                    }
                ]
            }
        """.trimIndent()

        val snapshot = SnapshotParser().parse(snapshotJson)
        val manager = OfflineAuthManager.create(context)

        val result = manager.sync(snapshot)

        assertTrue(result.successfulUsernames.isEmpty())
        assertEquals(1, result.failures.size)
        assertEquals("sync_test_bob", result.failures[0].username)

        val reason = result.failures[0].reason

        assertTrue(reason is CredentialSyncFailureReason.Backend)
        assertEquals(
            "unusable_password",
            (reason as CredentialSyncFailureReason.Backend).reason
        )
    }

    @Test
    fun unsupportedProtocolVersionIsRejected() = runBlocking {
        val snapshotJson = """
            {
                "protocol_version": 999,
                "credentials": [],
                "failures": []
            }
        """.trimIndent()

        val snapshot = SnapshotParser().parse(snapshotJson)
        val manager = OfflineAuthManager.create(context)

        try {
            manager.sync(snapshot)
            fail("Expected unsupported protocol exception")
        } catch (e: UnsupportedProtocolVersionException) {
            assertEquals(999, e.protocolVersion)
        }
    }

    @Test
    fun malformedSnapshotJsonIsRejected() {
        val invalidJson = """
            {
                "protocol_version": 1,
                "credentials": "invalid",
                "failures": []
            }
        """.trimIndent()

        try {
            SnapshotParser().parse(invalidJson)
            fail("Expected snapshot parsing to fail")
        } catch (e: kotlinx.serialization.SerializationException) {
            assertTrue(e.message?.isNotBlank() == true)
        }
    }
}
