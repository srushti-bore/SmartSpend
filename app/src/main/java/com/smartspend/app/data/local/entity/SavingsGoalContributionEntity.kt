package com.smartspend.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.smartspend.app.domain.model.ContributionType
import java.math.BigDecimal

@Entity(
    tableName = "savings_goal_contributions",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SavingsGoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["profileId"]),
        Index(value = ["goalId"]),
        Index(value = ["profileId", "goalId"])
    ]
)
data class SavingsGoalContributionEntity(
    @PrimaryKey val id: String,
    val goalId: String,
    val profileId: String,
    val amount: BigDecimal,
    val type: ContributionType,
    val date: Long,
    val notes: String?,
    val createdAt: Long
)
