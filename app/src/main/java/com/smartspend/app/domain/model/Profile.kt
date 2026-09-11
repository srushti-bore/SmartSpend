package com.smartspend.app.domain.model

enum class AuthType {
    PIN,
    PASSWORD,
    PATTERN
}

data class Profile(
    val id: String,
    val name: String,
    val primaryAuthType: AuthType,
    val credentialSalt: String,
    val credentialHash: String,
    val biometricEnabled: Boolean,
    val createdAt: Long
)
