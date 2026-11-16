
package expo.modules.offlineauth

import android.util.Base64
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.johndembaremba.offlineauth.OfflineAuthManager
import io.github.johndembaremba.offlineauth.serialization.SnapshotParser
import io.github.johndembaremba.offlineauth.testing.Pbkdf2TestHelper
import java.security.KeyFactory
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.MGF1ParameterSpec
import java.security.spec.X509EncodedKeySpec
import java.time.Instant
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OfflineAuthAuthenticationFlowTest {

    private val context
        get() = InstrumentationRegistry
            .getInstrumentation()
            .targetContext

    @Test
    fun syncEncryptedCredentialAndAuthenticateOffline() = runBlocking {
        val manager = OfflineAuthManager.create(context)

        val username = "auth_flow_${UUID.randomUUID()}"
        val correctPassword = "integration-test-password"

        // Step 1: Retrieve the device RSA public key.
        val publicKey = decodePublicKey(manager.getPublicKey())

        // Step 2: Generate a Django-compatible verifier.
        val verifier = Pbkdf2TestHelper.createVerifier(
            password = correctPassword,
            salt = "authentication-flow-test-salt",
            iterations = 100_000
        )

        val now = Instant.now()

        val credentialJson = """
            {
                "password_algorithm": "pbkdf2_sha256",
                "password_verifier": "$verifier",
                "credential_version": 1,
                "issued_at": "${now.minusSeconds(60)}",
                "expires_at": "${now.plusSeconds(3600)}"
            }
        """.trimIndent()

        // Step 3: Encrypt the credential for this device.
        val envelope = encryptCredential(
            publicKey = publicKey,
            credentialJson = credentialJson
        )

        // Step 4: Construct the authoritative snapshot.
        val snapshotJson = """
            {
                "protocol_version": 1,
                "credentials": [
                    {
                        "user_id": "authentication-flow-test",
                        "username": "$username",
                        "envelope": {
                            "key_algorithm": "RSA-OAEP-256",
                            "content_algorithm": "A256GCM",
                            "encrypted_key": "${envelope.encryptedKey}",
                            "nonce": "${envelope.nonce}",
                            "ciphertext": "${envelope.ciphertext}"
                        }
                    }
                ],
                "failures": []
            }
        """.trimIndent()

        // Step 5: Synchronize and securely store the credential.
        val snapshot = SnapshotParser().parse(snapshotJson)

        val result = manager.sync(snapshot)

        assertEquals(
            listOf(username),
            result.successfulUsernames
        )

        assertTrue(result.failures.isEmpty())

        // Step 6: Correct password must authenticate.
        assertTrue(
            authenticateSafely(
                manager = manager,
                username = username,
                password = correctPassword
            )
        )

        // Step 7: Incorrect password must fail.
        assertFalse(
            authenticateSafely(
                manager = manager,
                username = username,
                password = "wrong-password"
            )
        )

        // Step 8: Credentials must survive manager recreation.
        val recreatedManager = OfflineAuthManager.create(context)

        assertTrue(
            authenticateSafely(
                manager = recreatedManager,
                username = username,
                password = correctPassword
            )
        )
    }

    private fun authenticateSafely(
        manager: OfflineAuthManager,
        username: String,
        password: String
    ): Boolean {
        val passwordChars = password.toCharArray()

        return try {
            manager.authenticate(
                username = username,
                password = passwordChars
            )
        } finally {
            passwordChars.fill('\u0000')
        }
    }

    private fun decodePublicKey(encoded: String): PublicKey {
        val bytes = Base64.decode(encoded, Base64.NO_WRAP)

        return KeyFactory.getInstance("RSA").generatePublic(
            X509EncodedKeySpec(bytes)
        )
    }

    private fun encryptCredential(
        publicKey: PublicKey,
        credentialJson: String
    ): EncryptedEnvelopeData {
        val aesKey = KeyGenerator.getInstance("AES").apply {
            init(256)
        }.generateKey()

        val nonce = ByteArray(12).also {
            SecureRandom().nextBytes(it)
        }

        val contentCipher = Cipher.getInstance(
            "AES/GCM/NoPadding"
        )

        contentCipher.init(
            Cipher.ENCRYPT_MODE,
            aesKey,
            GCMParameterSpec(128, nonce)
        )

        val plaintext = credentialJson.toByteArray(Charsets.UTF_8)

        val ciphertext = try {
            contentCipher.doFinal(plaintext)
        } finally {
            plaintext.fill(0)
        }

        val keyCipher = Cipher.getInstance(
            "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
        )

        keyCipher.init(
            Cipher.ENCRYPT_MODE,
            publicKey,
            OAEPParameterSpec(
                "SHA-256",
                "MGF1",
                MGF1ParameterSpec.SHA1,
                PSource.PSpecified.DEFAULT
            )
        )

        val aesKeyBytes = aesKey.encoded

        val encryptedKey = try {
            keyCipher.doFinal(aesKeyBytes)
        } finally {
            aesKeyBytes.fill(0)
        }

        return EncryptedEnvelopeData(
            encryptedKey = Base64.encodeToString(
                encryptedKey,
                Base64.NO_WRAP
            ),
            nonce = Base64.encodeToString(
                nonce,
                Base64.NO_WRAP
            ),
            ciphertext = Base64.encodeToString(
                ciphertext,
                Base64.NO_WRAP
            )
        )
    }

    private data class EncryptedEnvelopeData(
        val encryptedKey: String,
        val nonce: String,
        val ciphertext: String
    )
}
