package expo.modules.offlineauth

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.johndembaremba.offlineauth.OfflineAuthManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OfflineAuthIntegrationTest {

    private val context
        get() = InstrumentationRegistry
            .getInstrumentation()
            .targetContext

    @Test
    fun getPublicKeyReturnsNonEmptyKey() {
        val manager = OfflineAuthManager.create(context)

        val publicKey = manager.getPublicKey()

        assertFalse("Public key must not be empty", publicKey.isBlank())
    }

    @Test
    fun repeatedCallsReturnSamePublicKey() {
        val manager = OfflineAuthManager.create(context)

        val first = manager.getPublicKey()
        val second = manager.getPublicKey()

        assertEquals(first, second)
    }

    @Test
    fun recreatingManagerPreservesPublicKey() {
        val firstManager = OfflineAuthManager.create(context)
        val firstKey = firstManager.getPublicKey()

        val secondManager = OfflineAuthManager.create(context)
        val secondKey = secondManager.getPublicKey()

        assertEquals(firstKey, secondKey)
    }
}
