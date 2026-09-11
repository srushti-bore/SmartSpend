package com.smartspend.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(
    tableName = "savings_goals",
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
        Index(value = ["profileId", "isArchived"])
    ]
)
data class SavingsGoalEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val name: String,
    val targetAmount: BigDecimal,
    val currentAmount: BigDecimal,
    val currency: String,
    val targetDate: Long,
    val color: String?,
    val icon: String?,
    val isArchived: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
