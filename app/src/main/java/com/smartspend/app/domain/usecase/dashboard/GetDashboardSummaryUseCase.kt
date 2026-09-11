package com.smartspend.app.domain.usecase.dashboard

import com.smartspend.app.core.money.MoneyUtils
import com.smartspend.app.data.local.dao.CategorySpendAggregate
import com.smartspend.app.domain.model.Budget
import com.smartspend.app.domain.model.BudgetProgress
import com.smartspend.app.domain.model.BudgetStatus
import com.smartspend.app.domain.model.BudgetType
import com.smartspend.app.domain.model.Expense
import com.smartspend.app.domain.repository.BudgetRepository
import com.smartspend.app.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.math.BigDecimal
import java.util.Calendar
import javax.inject.Inject

data class DashboardSummary(
    val totalSpentCurrentMonth: BigDecimal,
    val overallBudgetProgress: BudgetProgress?,
    val categoryBreakdown: List<CategorySpendAggregate>,
    val recentExpenses: List<Expense>
)

class GetDashboardSummaryUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val budgetRepository: BudgetRepository
) {
    operator fun invoke(profileId: String): Flow<DashboardSummary> {
        val calendar = Calendar.getInstance()
        val currentMonthEnd = calendar.timeInMillis
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val currentMonthStart = calendar.timeInMillis

        val totalSpentFlow = expenseRepository.getTotalSpending(profileId, currentMonthStart, currentMonthEnd)
        val recentExpensesFlow = expenseRepository.getRecentExpenses(profileId, limit = 5)
        val categoryBreakdownFlow = expenseRepository.getCategorySpending(profileId, currentMonthStart, currentMonthEnd)
        val monthlyBudgetFlow = budgetRepository.getOverallBudgetFlow(profileId, BudgetType.MONTHLY)

        return combine(
            totalSpentFlow,
            monthlyBudgetFlow,
            categoryBreakdownFlow,
            recentExpensesFlow
        ) { totalSpent, budget, categories, recent ->
            val spent = totalSpent ?: BigDecimal.ZERO

            val budgetProgress = budget?.let { b ->
                val remaining = b.amount.subtract(spent)
                val pct = MoneyUtils.calculatePercentage(spent, b.amount)
                val status = when {
                    spent.compareTo(b.amount) > 0 -> BudgetStatus.OVER_BUDGET
                    pct >= b.thresholdPct -> BudgetStatus.NEAR_LIMIT
                    else -> BudgetStatus.ON_TRACK
                }
                BudgetProgress(
                    budget = b,
                    spentAmount = spent,
                    remainingAmount = remaining,
                    percentageUsed = pct,
                    status = status
                )
            }

            DashboardSummary(
                totalSpentCurrentMonth = spent,
                overallBudgetProgress = budgetProgress,
                categoryBreakdown = categories,
                recentExpenses = recent
            )
        }
    }
}
