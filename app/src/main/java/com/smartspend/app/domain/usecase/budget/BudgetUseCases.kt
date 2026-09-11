package com.smartspend.app.domain.usecase.budget

import com.smartspend.app.core.money.MoneyUtils
import com.smartspend.app.domain.model.Budget
import com.smartspend.app.domain.model.BudgetProgress
import com.smartspend.app.domain.model.BudgetStatus
import com.smartspend.app.domain.model.BudgetType
import com.smartspend.app.domain.repository.BudgetRepository
import com.smartspend.app.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

class GetBudgetsUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    operator fun invoke(profileId: String): Flow<List<Budget>> {
        return budgetRepository.getBudgets(profileId)
    }
}

class UpsertBudgetUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(
        profileId: String,
        type: BudgetType,
        amount: BigDecimal,
        categoryId: String? = null,
        thresholdPct: Int = 80
    ): Result<Budget> {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.failure(IllegalArgumentException("Budget amount must be greater than zero"))
        }

        val existing = budgetRepository.getBudget(profileId, type, categoryId)
        val budget = Budget(
            id = existing?.id ?: UUID.randomUUID().toString(),
            profileId = profileId,
            type = type,
            amount = amount,
            categoryId = categoryId,
            thresholdPct = thresholdPct
        )

        budgetRepository.upsertBudget(budget)
        return Result.success(budget)
    }
}

class CalculateBudgetProgressUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    operator fun invoke(profileId: String, budget: Budget): Flow<BudgetProgress> {
        val (startDate, endDate) = getPeriodDates(budget.type)
        val totalSpentFlow = if (budget.categoryId != null) {
            expenseRepository.getCategoryTotalSpending(profileId, budget.categoryId, startDate, endDate)
        } else {
            expenseRepository.getTotalSpending(profileId, startDate, endDate)
        }

        return totalSpentFlow.combine(flowOf(budget)) { spent, b ->
            val spentSafe = spent ?: BigDecimal.ZERO
            val remaining = b.amount.subtract(spentSafe)
            val pct = MoneyUtils.calculatePercentage(spentSafe, b.amount)

            val status = when {
                spentSafe.compareTo(b.amount) > 0 -> BudgetStatus.OVER_BUDGET
                pct >= b.thresholdPct -> BudgetStatus.NEAR_LIMIT
                else -> BudgetStatus.ON_TRACK
            }

            BudgetProgress(
                budget = b,
                spentAmount = spentSafe,
                remainingAmount = remaining,
                percentageUsed = pct,
                status = status
            )
        }
    }

    private fun getPeriodDates(type: BudgetType): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val end = calendar.timeInMillis

        when (type) {
            BudgetType.DAILY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            BudgetType.WEEKLY -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            BudgetType.MONTHLY, BudgetType.CATEGORY -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            BudgetType.YEARLY -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
        }
        val start = calendar.timeInMillis
        return Pair(start, end)
    }
}
