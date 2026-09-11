package com.smartspend.app.domain.intelligence

import com.smartspend.app.domain.repository.AccountRepository
import com.smartspend.app.domain.repository.BudgetRepository
import com.smartspend.app.domain.repository.ExpenseRepository
import com.smartspend.app.domain.repository.IncomeRepository
import com.smartspend.app.domain.repository.RecurringExpenseRepository
import kotlinx.coroutines.flow.first
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

data class HealthPillar(
    val name: String,
    val score: Int,       // 0 - 20
    val maxScore: Int = 20,
    val status: String,
    val description: String
)

data class FinancialHealthReport(
    val overallScore: Int, // 0 - 100
    val grade: String,     // A+, A, B, C, D
    val summaryTitle: String,
    val pillars: List<HealthPillar>,
    val actionRecommendations: List<String>
)

@Singleton
class FinancialHealthScoreUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
    private val budgetRepository: BudgetRepository,
    private val accountRepository: AccountRepository,
    private val recurringRepository: RecurringExpenseRepository
) {

    suspend operator fun invoke(profileId: String): FinancialHealthReport {
        val calendar = Calendar.getInstance()
        val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

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
        val accounts = accountRepository.getAccounts(profileId).first()
        val recurring = recurringRepository.getActiveRecurringExpenses(profileId).first()

        // 1. Pillar 1: Savings Discipline (0 - 20 pts)
        val netSavings = totalIncome.subtract(totalSpent)
        val savingsRate = if (totalIncome > BigDecimal.ZERO) {
            netSavings.multiply(BigDecimal("100")).divide(totalIncome, 1, RoundingMode.HALF_EVEN).toDouble()
        } else 0.0

        val savingsScore = when {
            savingsRate >= 25.0 -> 20
            savingsRate >= 15.0 -> 16
            savingsRate >= 5.0 -> 12
            savingsRate >= 0.0 -> 8
            else -> 2
        }
        val savingsPillar = HealthPillar(
            name = "Savings Discipline",
            score = savingsScore,
            status = if (savingsScore >= 16) "Excellent" else if (savingsScore >= 10) "Fair" else "Critical",
            description = "${String.format("%.1f", savingsRate)}% of monthly income saved."
        )

        // 2. Pillar 2: Budget Adherence (0 - 20 pts)
        val budgetAmount = overallBudget?.amount ?: BigDecimal("30000.00")
        val budgetPctUsed = if (budgetAmount > BigDecimal.ZERO) {
            totalSpent.multiply(BigDecimal("100")).divide(budgetAmount, 0, RoundingMode.HALF_EVEN).toInt()
        } else 0

        val budgetScore = when {
            budgetPctUsed <= 70 -> 20
            budgetPctUsed <= 85 -> 17
            budgetPctUsed <= 95 -> 13
            budgetPctUsed <= 100 -> 9
            else -> 2
        }
        val budgetPillar = HealthPillar(
            name = "Budget Adherence",
            score = budgetScore,
            status = if (budgetScore >= 17) "On Track" else if (budgetScore >= 10) "Tight" else "Over Budget",
            description = "$budgetPctUsed% of monthly budget consumed."
        )

        // 3. Pillar 3: Spending Stability (0 - 20 pts)
        val daysElapsed = maxOf(1, calendar.get(Calendar.DAY_OF_MONTH))
        val expectedPct = (daysElapsed * 100) / maxDays
        val velocityDiff = budgetPctUsed - expectedPct

        val stabilityScore = when {
            velocityDiff <= 0 -> 20
            velocityDiff <= 10 -> 16
            velocityDiff <= 20 -> 11
            else -> 5
        }
        val stabilityPillar = HealthPillar(
            name = "Spending Stability",
            score = stabilityScore,
            status = if (stabilityScore >= 16) "Predictable" else "Erratic",
            description = if (velocityDiff <= 0) "Spending velocity is safely under the calendar run-rate." else "Spending pacing is ahead of target by $velocityDiff%."
        )

        // 4. Pillar 4: Cash Cushion / Liquidity (0 - 20 pts)
        val totalLiquidBalance = accounts.map { it.currentBalance }.fold(BigDecimal.ZERO) { acc, b -> acc.add(b) }
        val monthlyBurn = if (totalSpent > BigDecimal.ZERO) totalSpent else BigDecimal("10000.00")
        val cushionRatio = if (monthlyBurn > BigDecimal.ZERO) {
            totalLiquidBalance.divide(monthlyBurn, 1, RoundingMode.HALF_EVEN).toDouble()
        } else 1.0

        val cushionScore = when {
            cushionRatio >= 3.0 -> 20
            cushionRatio >= 1.5 -> 16
            cushionRatio >= 0.8 -> 12
            cushionRatio >= 0.3 -> 7
            else -> 3
        }
        val cushionPillar = HealthPillar(
            name = "Cash Cushion",
            score = cushionScore,
            status = if (cushionScore >= 16) "Robust" else "Low Reserve",
            description = "${String.format("%.1f", cushionRatio)}x monthly expense buffer in recorded wallets."
        )

        // 5. Pillar 5: Leak Control (0 - 20 pts)
        val totalSubscriptionLoad = recurring.map { it.amount }.fold(BigDecimal.ZERO) { acc, b -> acc.add(b) }
        val subRatio = if (totalIncome > BigDecimal.ZERO) {
            totalSubscriptionLoad.multiply(BigDecimal("100")).divide(totalIncome, 1, RoundingMode.HALF_EVEN).toDouble()
        } else 5.0

        val leakScore = when {
            subRatio <= 8.0 -> 20
            subRatio <= 15.0 -> 16
            subRatio <= 25.0 -> 11
            else -> 4
        }
        val leakPillar = HealthPillar(
            name = "Leak Control",
            score = leakScore,
            status = if (leakScore >= 16) "Optimized" else "High Fixed Load",
            description = "Recurring commitments form ${String.format("%.1f", subRatio)}% of monthly cash flow."
        )

        val pillars = listOf(savingsPillar, budgetPillar, stabilityPillar, cushionPillar, leakPillar)
        val totalScore = pillars.sumOf { it.score }

        val grade = when {
            totalScore >= 90 -> "A+"
            totalScore >= 80 -> "A"
            totalScore >= 70 -> "B"
            totalScore >= 55 -> "C"
            else -> "D"
        }

        val summaryTitle = when (grade) {
            "A+", "A" -> "Exceptional Financial Discipline 🏆"
            "B" -> "Solid & Healthy Financial Trajectory 📈"
            "C" -> "Fair — Action Needed on Discretionary Spends ⚠️"
            else -> "Vulnerable — Immediate Spending Freeze Advised 🚨"
        }

        val recommendations = mutableListOf<String>()
        if (savingsScore < 16) recommendations.add("Increase monthly savings rate to at least 15% by cutting non-essential dining/shopping.")
        if (budgetScore < 15) recommendations.add("Stay within the daily safe-to-spend limit to prevent budget overrun.")
        if (leakScore < 15) recommendations.add("Review active recurring subscriptions in Leak Hunter to eliminate unused services.")
        if (recommendations.isEmpty()) {
            recommendations.add("Keep maintaining your consistent savings pace!")
            recommendations.add("Consider allocating extra net cash flow towards long-term savings goals.")
        }

        return FinancialHealthReport(
            overallScore = totalScore,
            grade = grade,
            summaryTitle = summaryTitle,
            pillars = pillars,
            actionRecommendations = recommendations.take(3)
        )
    }
}
