package com.smartspend.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.smartspend.app.domain.model.BudgetType
import java.math.BigDecimal

@Entity(
    tableName = "budgets",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["profileId"]),
        Index(value = ["categoryId"]),
        Index(value = ["profileId", "type", "categoryId"], unique = true)
    ]
)
data class BudgetEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val type: BudgetType,
    val amount: BigDecimal,
    val categoryId: String?,
    val thresholdPct: Int
)
