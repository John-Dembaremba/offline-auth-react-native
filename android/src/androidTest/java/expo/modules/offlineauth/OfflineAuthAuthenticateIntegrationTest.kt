package expo.modules.offlineauth

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.johndembaremba.offlineauth.OfflineAuthManager
import java.util.UUID
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OfflineAuthAuthenticateIntegrationTest {

    private val context
        get() = InstrumentationRegistry
            .getInstrumentation()
            .targetContext

    @Test
    fun unknownUserCannotAuthenticate() {
        val manager = OfflineAuthManager.create(context)

        val username = "unknown_${UUID.randomUUID()}"
        val password = "test-password".toCharArray()

        try {
            val result = manager.authenticate(
                username = username,
                password = password
            )

            assertFalse(result)
        } finally {
            password.fill('\u0000')
        }
    }
}
