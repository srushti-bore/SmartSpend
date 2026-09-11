package com.smartspend.app.core.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HashUtilsTest {

    @Test
    fun `generateSalt generates non-empty distinct salts`() {
        val salt1 = HashUtils.generateSalt()
        val salt2 = HashUtils.generateSalt()
        assertTrue(salt1.isNotEmpty())
        assertTrue(salt2.isNotEmpty())
        assertNotEquals(salt1, salt2)
    }

    @Test
    fun `verifyCredential returns true for matching password and false for wrong password`() {
        val salt = HashUtils.generateSalt()
        val pin = "1234"
        val hash = HashUtils.hashCredential(pin, salt)

        assertTrue(HashUtils.verifyCredential("1234", salt, hash))
        assertFalse(HashUtils.verifyCredential("9999", salt, hash))
        assertFalse(HashUtils.verifyCredential("12345", salt, hash))
    }
}
