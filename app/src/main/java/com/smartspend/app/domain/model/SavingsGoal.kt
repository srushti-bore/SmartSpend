package com.smartspend.app.domain.model

import java.math.BigDecimal

enum class ContributionType {
    DEPOSIT,
    WITHDRAWAL
}

data class SavingsGoalContribution(
    val id: String,
    val goalId: String,
    val profileId: String,
    val amount: BigDecimal,
    val type: ContributionType = ContributionType.DEPOSIT,
    val date: Long = System.currentTimeMillis(),
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class SavingsGoal(
    val id: String,
    val profileId: String,
    val name: String,
    val targetAmount: BigDecimal,
    val currentAmount: BigDecimal = BigDecimal.ZERO,
    val currency: String = "INR",
    val targetDate: Long,
    val color: String? = null,
    val icon: String? = null,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val progressPct: Float
        get() = if (targetAmount > BigDecimal.ZERO) {
            (currentAmount.toDouble() / targetAmount.toDouble()).coerceIn(0.0, 1.0).toFloat()
        } else 0f

    val isReached: Boolean
        get() = currentAmount >= targetAmount
}
