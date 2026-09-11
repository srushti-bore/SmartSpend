package com.smartspend.app.domain.intelligence

import com.smartspend.app.domain.model.CashFlowSummary
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

enum class SafeSpendTier {
    HEALTHY,    // < 60% spent
    MODERATE,   // 60% - 79% spent
    CAUTION,    // 80% - 99% spent
    DANGER      // >= 100% or deficit
}

data class SafeToSpendResult(
    val safeDailySpend: BigDecimal,
    val safeWeeklySpend: BigDecimal,
    val remainingBudget: BigDecimal,
    val totalSpent: BigDecimal,
    val totalBudget: BigDecimal,
    val daysRemainingInMonth: Int,
    val upcomingRecurringCommitments: BigDecimal,
    val statusTier: SafeSpendTier,
    val percentageUsed: Int,
    val headlineMessage: String,
    val pacingAdvice: String
)

@Singleton
class SafeToSpendEngine @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val budgetRepository: BudgetRepository,
    private val recurringRepository: RecurringExpenseRepository,
    private val incomeRepository: IncomeRepository
) {

    suspend fun calculate(profileId: String): SafeToSpendResult {
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val daysRemaining = maxOf(1, maxDays - currentDay + 1)

        // Start & End of current month
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
        val overallBudget = budgetRepository.getOverallBudget(profileId).first()
        val activeSubscriptions = recurringRepository.getActiveRecurringExpenses(profileId).first()

        val totalBudget = overallBudget?.amount ?: BigDecimal("30000.00")
        val remainingBudget = totalBudget.subtract(totalSpent)

        // Upcoming recurring commitments in remainder of month
        val upcomingRecurring = activeSubscriptions
            .filter { it.nextDueDate in System.currentTimeMillis()..endCal.timeInMillis }
            .map { it.amount }
            .fold(BigDecimal.ZERO) { acc, amt -> acc.add(amt) }

        val netAvailableForDiscretionary = remainingBudget.subtract(upcomingRecurring)

        val safeDaily = if (netAvailableForDiscretionary > BigDecimal.ZERO) {
            netAvailableForDiscretionary.divide(BigDecimal(daysRemaining), 2, RoundingMode.HALF_EVEN)
        } else {
            BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN)
        }

        val safeWeekly = safeDaily.multiply(BigDecimal("7.00")).setScale(2, RoundingMode.HALF_EVEN)

        val pctUsed = if (totalBudget > BigDecimal.ZERO) {
            totalSpent.multiply(BigDecimal("100"))
                .divide(totalBudget, 0, RoundingMode.HALF_EVEN)
                .toInt()
        } else 0

        val tier = when {
            pctUsed < 60 -> SafeSpendTier.HEALTHY
            pctUsed in 60..79 -> SafeSpendTier.MODERATE
            pctUsed in 80..99 -> SafeSpendTier.CAUTION
            else -> SafeSpendTier.DANGER
        }

        val headline = when (tier) {
            SafeSpendTier.HEALTHY -> "Safe Run-Rate: Strong Pacing 🚀"
            SafeSpendTier.MODERATE -> "Balanced Spending: On Track ⚖️"
            SafeSpendTier.CAUTION -> "Watch Out: Budget Tightening ⚠️"
            SafeSpendTier.DANGER -> "Over-Budget / Deficit Warning 🚨"
        }

        val advice = when (tier) {
            SafeSpendTier.HEALTHY -> "You have ₹$safeDaily safe daily spend for the remaining $daysRemaining days."
            SafeSpendTier.MODERATE -> "Maintain ₹$safeDaily/day to finish the month comfortably under budget."
            SafeSpendTier.CAUTION -> "Pace discretionary spends at ₹$safeDaily/day to avoid exceeding your budget."
            SafeSpendTier.DANGER -> "You have consumed 100%+ of your budget. Pause discretionary purchases."
        }

        return SafeToSpendResult(
            safeDailySpend = safeDaily,
            safeWeeklySpend = safeWeekly,
            remainingBudget = remainingBudget,
            totalSpent = totalSpent,
            totalBudget = totalBudget,
            daysRemainingInMonth = daysRemaining,
            upcomingRecurringCommitments = upcomingRecurring,
            statusTier = tier,
            percentageUsed = pctUsed,
            headlineMessage = headline,
            pacingAdvice = advice
        )
    }
}
