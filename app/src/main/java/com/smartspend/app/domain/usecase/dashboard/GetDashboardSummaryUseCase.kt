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
    val dailyBudgetProgress: BudgetProgress? = null,
    val categoryBreakdown: List<CategorySpendAggregate>,
    val recentExpenses: List<Expense>
)

class GetDashboardSummaryUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val budgetRepository: BudgetRepository
) {
    operator fun invoke(profileId: String): Flow<DashboardSummary> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val currentMonthStart = calendar.timeInMillis

        calendar.add(Calendar.MONTH, 1)
        val currentMonthEnd = calendar.timeInMillis - 1

        val todayCal = Calendar.getInstance()
        todayCal.set(Calendar.HOUR_OF_DAY, 0)
        todayCal.set(Calendar.MINUTE, 0)
        todayCal.set(Calendar.SECOND, 0)
        todayCal.set(Calendar.MILLISECOND, 0)
        val todayStart = todayCal.timeInMillis
        val todayEnd = todayStart + (24 * 60 * 60 * 1000) - 1

        val totalSpentFlow = expenseRepository.getTotalSpending(profileId, currentMonthStart, currentMonthEnd)
        val todaySpentFlow = expenseRepository.getTotalSpending(profileId, todayStart, todayEnd)
        val recentExpensesFlow = expenseRepository.getRecentExpenses(profileId, limit = 5)
        val categoryBreakdownFlow = expenseRepository.getCategorySpending(profileId, currentMonthStart, currentMonthEnd)
        val monthlyBudgetFlow = budgetRepository.getOverallBudgetFlow(profileId, BudgetType.MONTHLY)
        val dailyBudgetFlow = budgetRepository.getOverallBudgetFlow(profileId, BudgetType.DAILY)

        return combine(
            combine(totalSpentFlow, todaySpentFlow, monthlyBudgetFlow) { tSpent, tdSpent, mBudget ->
                Triple(tSpent, tdSpent, mBudget)
            },
            dailyBudgetFlow,
            categoryBreakdownFlow,
            recentExpensesFlow
        ) { (totalSpent, todaySpent, monthlyBudget), dailyBudget, categories, recent ->
            val spent = totalSpent ?: BigDecimal.ZERO
            val spentToday = todaySpent ?: BigDecimal.ZERO

            val budgetProgress = monthlyBudget?.let { b ->
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

            val dailyProgress = dailyBudget?.let { b ->
                val remaining = b.amount.subtract(spentToday)
                val pct = MoneyUtils.calculatePercentage(spentToday, b.amount)
                val status = when {
                    spentToday.compareTo(b.amount) > 0 -> BudgetStatus.OVER_BUDGET
                    pct >= b.thresholdPct -> BudgetStatus.NEAR_LIMIT
                    else -> BudgetStatus.ON_TRACK
                }
                BudgetProgress(
                    budget = b,
                    spentAmount = spentToday,
                    remainingAmount = remaining,
                    percentageUsed = pct,
                    status = status
                )
            }

            DashboardSummary(
                totalSpentCurrentMonth = spent,
                overallBudgetProgress = budgetProgress,
                dailyBudgetProgress = dailyProgress,
                categoryBreakdown = categories,
                recentExpenses = recent
            )
        }
    }
}
