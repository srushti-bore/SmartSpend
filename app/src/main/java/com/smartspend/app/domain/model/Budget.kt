package com.smartspend.app.domain.model

import java.math.BigDecimal

enum class BudgetType {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY,
    CATEGORY
}

enum class BudgetStatus {
    ON_TRACK,
    NEAR_LIMIT,
    OVER_BUDGET
}

data class Budget(
    val id: String,
    val profileId: String,
    val type: BudgetType,
    val amount: BigDecimal,
    val categoryId: String? = null,
    val thresholdPct: Int = 80 // Default ~80% per SRS FR-P1-009
)

data class BudgetProgress(
    val budget: Budget,
    val spentAmount: BigDecimal,
    val remainingAmount: BigDecimal,
    val percentageUsed: Int,
    val status: BudgetStatus
)
