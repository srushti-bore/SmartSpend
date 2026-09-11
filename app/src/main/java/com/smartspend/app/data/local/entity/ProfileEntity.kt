package com.smartspend.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.smartspend.app.domain.model.AuthType

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val primaryAuthType: AuthType,
    val credentialSalt: String,
    val credentialHash: String,
    val biometricEnabled: Boolean,
    val createdAt: Long
)
