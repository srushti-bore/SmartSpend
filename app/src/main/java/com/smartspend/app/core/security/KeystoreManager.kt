package com.smartspend.app.core.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import java.util.Base64

open class KeystoreManager {

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "SmartSpendMasterKey"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128
        private const val SALT_LENGTH = 16
        private const val PBKDF2_ITERATIONS = 65536
        private const val PBKDF2_KEY_LENGTH = 256
        private const val PORTABLE_PREFIX = "SPEND_ENC_V2:"
        private const val PASSWORD_PREFIX = "SPEND_ENC_V3_PWD:"
        private val DERIVATION_SECRET = "SmartSpend_Ledger_AES256_GCM_Vault".toCharArray()
    }

    private val keyStore: KeyStore? by lazy {
        try {
            KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        } catch (_: Exception) {
            null
        }
    }

    init {
        try {
            createKeyIfNeeded()
        } catch (_: Exception) {
            // Gracefully handled in mock/JVM test environments
        }
    }

    private fun createKeyIfNeeded() {
        val ks = keyStore ?: return
        if (!ks.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            keyGenerator.init(spec)
            keyGenerator.generateKey()
        }
    }

    private fun getSecretKey(): SecretKey? {
        return try {
            keyStore?.getKey(KEY_ALIAS, null) as? SecretKey
        } catch (_: Exception) {
            null
        }
    }

    private fun deriveKey(salt: ByteArray, passphrase: CharArray = DERIVATION_SECRET): SecretKey {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(passphrase, salt, PBKDF2_ITERATIONS, PBKDF2_KEY_LENGTH)
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }

    fun isPasswordProtected(encryptedText: String): Boolean {
        return encryptedText.trim().startsWith(PASSWORD_PREFIX)
    }

    open fun encrypt(plainText: String, password: String? = null): String {
        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH).apply { random.nextBytes(this) }
        val iv = ByteArray(GCM_IV_LENGTH).apply { random.nextBytes(this) }

        val passphrase = if (!password.isNullOrBlank()) password.toCharArray() else DERIVATION_SECRET
        val prefix = if (!password.isNullOrBlank()) PASSWORD_PREFIX else PORTABLE_PREFIX

        val secretKey = deriveKey(salt, passphrase)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)
        val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        val combined = ByteArray(salt.size + iv.size + cipherBytes.size)
        System.arraycopy(salt, 0, combined, 0, salt.size)
        System.arraycopy(iv, 0, combined, salt.size, iv.size)
        System.arraycopy(cipherBytes, 0, combined, salt.size + iv.size, cipherBytes.size)

        return prefix + Base64.getEncoder().encodeToString(combined)
    }

    open fun decrypt(encryptedText: String, password: String? = null): String {
        val trimmed = encryptedText.trim()

        if (trimmed.startsWith(PASSWORD_PREFIX)) {
            if (password.isNullOrBlank()) {
                throw IllegalArgumentException("Password is required to decrypt this backup")
            }
            return tryDecryptWithPbe(
                rawBase64 = trimmed.removePrefix(PASSWORD_PREFIX).trim(),
                passphrase = password.toCharArray()
            )
        }

        if (trimmed.startsWith(PORTABLE_PREFIX)) {
            val passphrase = if (!password.isNullOrBlank()) password.toCharArray() else DERIVATION_SECRET
            return tryDecryptWithPbe(
                rawBase64 = trimmed.removePrefix(PORTABLE_PREFIX).trim(),
                passphrase = passphrase
            )
        }

        // Fallback for legacy AndroidKeyStore encrypted backups
        val key = getSecretKey() ?: throw IllegalStateException("Keystore key not available for legacy backup")
        val combined = Base64.getDecoder().decode(trimmed)
        val iv = ByteArray(GCM_IV_LENGTH)
        val encryptedBytes = ByteArray(combined.size - GCM_IV_LENGTH)
        System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH)
        System.arraycopy(combined, GCM_IV_LENGTH, encryptedBytes, 0, encryptedBytes.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    private fun tryDecryptWithPbe(rawBase64: String, passphrase: CharArray): String {
        try {
            val combined = Base64.getDecoder().decode(rawBase64)
            val salt = ByteArray(SALT_LENGTH)
            val iv = ByteArray(GCM_IV_LENGTH)
            val cipherBytes = ByteArray(combined.size - SALT_LENGTH - GCM_IV_LENGTH)

            System.arraycopy(combined, 0, salt, 0, SALT_LENGTH)
            System.arraycopy(combined, SALT_LENGTH, iv, 0, GCM_IV_LENGTH)
            System.arraycopy(combined, SALT_LENGTH + GCM_IV_LENGTH, cipherBytes, 0, cipherBytes.size)

            val secretKey = deriveKey(salt, passphrase)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            val plainBytes = cipher.doFinal(cipherBytes)
            return String(plainBytes, Charsets.UTF_8)
        } catch (_: AEADBadTagException) {
            throw IllegalArgumentException("Incorrect backup password. Please check and try again.")
        } catch (e: Exception) {
            if (e is IllegalArgumentException) throw e
            throw IllegalArgumentException("Decryption failed: ${e.localizedMessage ?: "Invalid file or password"}")
        }
    }
}
