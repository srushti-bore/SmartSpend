package com.smartspend.app.core.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object HashUtils {
    private const val SALT_BYTES = 16

    fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(SALT_BYTES)
        random.nextBytes(salt)
        return Base64.getEncoder().encodeToString(salt)
    }

    fun hashCredential(credential: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val combined = "$salt:$credential".toByteArray(Charsets.UTF_8)
        val hash = md.digest(combined)
        return Base64.getEncoder().encodeToString(hash)
    }

    fun verifyCredential(credential: String, salt: String, expectedHash: String): Boolean {
        val computedHash = hashCredential(credential, salt)
        return computedHash == expectedHash
    }
}
