package com.smartspend.app.domain.intelligence

import com.smartspend.app.domain.repository.BudgetRepository
import com.smartspend.app.domain.repository.CategoryRepository
import com.smartspend.app.domain.repository.ExpenseRepository
import com.smartspend.app.domain.repository.IncomeRepository
import kotlinx.coroutines.flow.first
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

data class Rule503020Split(
    val needsAmount: BigDecimal,
    val needsPct: Double,
    val wantsAmount: BigDecimal,
    val wantsPct: Double,
    val savingsAmount: BigDecimal,
    val savingsPct: Double
)

data class ForecastReport(
    val currentSpend: BigDecimal,
    val dailyBurnRate: BigDecimal,
    val projectedMonthEndSpend: BigDecimal,
    val budgetAmount: BigDecimal,
    val projectedDeficitOrSurplus: BigDecimal,
    val isProjectedOverBudget: Boolean,
    val rule503020: Rule503020Split,
    val summaryMessage: String
)

@Singleton
class SpendForecasterUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository
) {

    suspend operator fun invoke(profileId: String): ForecastReport {
        val calendar = Calendar.getInstance()
        val currentDay = maxOf(1, calendar.get(Calendar.DAY_OF_MONTH))
        val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val daysRemaining = maxOf(0, maxDays - currentDay)

        val startCal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endCal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, maxDays)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        val totalSpent = expenseRepository.getTotalSpending(profileId, startCal.timeInMillis, endCal.timeInMillis).first()
        val totalIncome = incomeRepository.getTotalIncome(profileId, startCal.timeInMillis, endCal.timeInMillis).first()
        val overallBudget = budgetRepository.getOverallBudget(profileId).first()
        val budgetAmount = overallBudget?.amount ?: BigDecimal("30000.00")
        val categoryBreakdown = expenseRepository.getCategorySpending(profileId, startCal.timeInMillis, endCal.timeInMillis).first()

        // Daily Burn Rate
        val burnRate = if (totalSpent > BigDecimal.ZERO) {
            totalSpent.divide(BigDecimal(currentDay), 2, RoundingMode.HALF_EVEN)
        } else BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN)

        val projectedExtra = burnRate.multiply(BigDecimal(daysRemaining)).setScale(2, RoundingMode.HALF_EVEN)
        val projectedMonthEnd = totalSpent.add(projectedExtra)

        val projectedDiff = budgetAmount.subtract(projectedMonthEnd)
        val isOverBudget = projectedDiff < BigDecimal.ZERO

        // 50/30/20 Needs vs Wants vs Savings
        val needsKeywords = listOf("housing", "rent", "bill", "utility", "utilities", "grocery", "groceries", "health", "education")
        var needsTotal = BigDecimal.ZERO
        var wantsTotal = BigDecimal.ZERO

        for (item in categoryBreakdown) {
            val lower = item.categoryName.lowercase()
            if (needsKeywords.any { lower.contains(it) }) {
                needsTotal = needsTotal.add(item.totalAmount)
            } else {
                wantsTotal = wantsTotal.add(item.totalAmount)
            }
        }

        val netSavings = maxOf(BigDecimal.ZERO, totalIncome.subtract(totalSpent))
        val totalFlow = totalIncome.max(totalSpent)

        val needsPct = if (totalFlow > BigDecimal.ZERO) {
            needsTotal.multiply(BigDecimal("100")).divide(totalFlow, 1, RoundingMode.HALF_EVEN).toDouble()
        } else 0.0

        val wantsPct = if (totalFlow > BigDecimal.ZERO) {
            wantsTotal.multiply(BigDecimal("100")).divide(totalFlow, 1, RoundingMode.HALF_EVEN).toDouble()
        } else 0.0

        val savingsPct = if (totalFlow > BigDecimal.ZERO) {
            netSavings.multiply(BigDecimal("100")).divide(totalFlow, 1, RoundingMode.HALF_EVEN).toDouble()
        } else 0.0

        val split = Rule503020Split(
            needsAmount = needsTotal,
            needsPct = needsPct,
            wantsAmount = wantsTotal,
            wantsPct = wantsPct,
            savingsAmount = netSavings,
            savingsPct = savingsPct
        )

        val summary = if (isOverBudget) {
            "At your current burn-rate of ₹$burnRate/day, month-end spending is projected to reach ₹$projectedMonthEnd, exceeding your budget by ₹${projectedDiff.abs()}."
        } else {
            "At your current burn-rate of ₹$burnRate/day, month-end spending will reach ₹$projectedMonthEnd, finishing safely with ₹$projectedDiff surplus."
        }

        return ForecastReport(
            currentSpend = totalSpent,
            dailyBurnRate = burnRate,
            projectedMonthEndSpend = projectedMonthEnd,
            budgetAmount = budgetAmount,
            projectedDeficitOrSurplus = projectedDiff,
            isProjectedOverBudget = isOverBudget,
            rule503020 = split,
            summaryMessage = summary
        )
    }
}
