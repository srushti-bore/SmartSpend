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
        val startCal = Calendar.getInstance()
        val endCal = Calendar.getInstance()

        when (type) {
            BudgetType.DAILY -> {
                startCal.set(Calendar.HOUR_OF_DAY, 0)
                startCal.set(Calendar.MINUTE, 0)
                startCal.set(Calendar.SECOND, 0)
                startCal.set(Calendar.MILLISECOND, 0)

                endCal.set(Calendar.HOUR_OF_DAY, 23)
                endCal.set(Calendar.MINUTE, 59)
                endCal.set(Calendar.SECOND, 59)
                endCal.set(Calendar.MILLISECOND, 999)
            }
            BudgetType.WEEKLY -> {
                startCal.set(Calendar.DAY_OF_WEEK, startCal.firstDayOfWeek)
                startCal.set(Calendar.HOUR_OF_DAY, 0)
                startCal.set(Calendar.MINUTE, 0)
                startCal.set(Calendar.SECOND, 0)
                startCal.set(Calendar.MILLISECOND, 0)

                endCal.timeInMillis = startCal.timeInMillis
                endCal.add(Calendar.DAY_OF_WEEK, 7)
                endCal.add(Calendar.MILLISECOND, -1)
            }
            BudgetType.MONTHLY, BudgetType.CATEGORY -> {
                startCal.set(Calendar.DAY_OF_MONTH, 1)
                startCal.set(Calendar.HOUR_OF_DAY, 0)
                startCal.set(Calendar.MINUTE, 0)
                startCal.set(Calendar.SECOND, 0)
                startCal.set(Calendar.MILLISECOND, 0)

                endCal.timeInMillis = startCal.timeInMillis
                endCal.add(Calendar.MONTH, 1)
                endCal.add(Calendar.MILLISECOND, -1)
            }
            BudgetType.YEARLY -> {
                startCal.set(Calendar.DAY_OF_YEAR, 1)
                startCal.set(Calendar.HOUR_OF_DAY, 0)
                startCal.set(Calendar.MINUTE, 0)
                startCal.set(Calendar.SECOND, 0)
                startCal.set(Calendar.MILLISECOND, 0)

                endCal.timeInMillis = startCal.timeInMillis
                endCal.add(Calendar.YEAR, 1)
                endCal.add(Calendar.MILLISECOND, -1)
            }
        }
        return Pair(startCal.timeInMillis, endCal.timeInMillis)
    }
}
