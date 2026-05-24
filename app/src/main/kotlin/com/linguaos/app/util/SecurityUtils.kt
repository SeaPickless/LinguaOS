package com.linguaos.app.util

import java.security.MessageDigest

object SecurityUtils {
    /**
     * Hash = SHA-256(password + username)  — username is the salt.
     */
    fun hashPassword(password: String, username: String): String {
        val input = password + username
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(password: String, username: String, storedHash: String): Boolean {
        return hashPassword(password, username) == storedHash
    }
}
