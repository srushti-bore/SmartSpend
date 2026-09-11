package com.smartspend.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.smartspend.app.domain.model.RecurringFrequency
import java.math.BigDecimal

@Entity(
    tableName = "recurring_expenses",
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
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = PaymentMethodEntity::class,
            parentColumns = ["id"],
            childColumns = ["paymentMethodId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["profileId"]),
        Index(value = ["categoryId"]),
        Index(value = ["paymentMethodId"]),
        Index(value = ["nextDueDate"]),
        Index(value = ["profileId", "isActive"])
    ]
)
data class RecurringExpenseEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val title: String,
    val amount: BigDecimal,
    val currency: String,
    val categoryId: String,
    val paymentMethodId: String,
    val frequency: RecurringFrequency,
    val startDate: Long,
    val nextDueDate: Long,
    val lastGeneratedDate: Long?,
    val isActive: Boolean,
    val autoCreate: Boolean,
    val notifyBeforeDays: Int,
    val notes: String?,
    val createdAt: Long,
    val updatedAt: Long
)
