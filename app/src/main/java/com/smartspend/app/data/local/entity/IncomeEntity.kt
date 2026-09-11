package com.smartspend.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.smartspend.app.domain.model.IncomeSource
import java.math.BigDecimal

@Entity(
    tableName = "incomes",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PaymentMethodEntity::class,
            parentColumns = ["id"],
            childColumns = ["paymentMethodId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["profileId"]),
        Index(value = ["paymentMethodId"]),
        Index(value = ["date"]),
        Index(value = ["profileId", "date"])
    ]
)
data class IncomeEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val source: IncomeSource,
    val title: String,
    val amount: BigDecimal,
    val currency: String,
    val date: Long,
    val paymentMethodId: String?,
    val notes: String?,
    val isRecurring: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
