package com.smartspend.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.smartspend.app.domain.model.AccountType
import java.math.BigDecimal

@Entity(
    tableName = "accounts",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["profileId"]),
        Index(value = ["profileId", "name"], unique = true)
    ]
)
data class AccountEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val name: String,
    val type: AccountType,
    val currency: String,
    val initialBalance: BigDecimal,
    val color: String?,
    val icon: String?,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
