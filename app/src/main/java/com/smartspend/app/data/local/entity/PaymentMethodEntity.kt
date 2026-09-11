package com.smartspend.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.smartspend.app.domain.model.PaymentType

@Entity(
    tableName = "payment_methods",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["profileId"])
    ]
)
data class PaymentMethodEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val type: PaymentType,
    val label: String,
    val isCustom: Boolean
)
