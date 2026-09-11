package com.smartspend.app.domain.intelligence

import com.smartspend.app.domain.repository.BudgetRepository
import com.smartspend.app.domain.repository.ExpenseRepository
import com.smartspend.app.domain.repository.RecurringExpenseRepository
import kotlinx.coroutines.flow.first
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

enum class SimulationDecision {
    SAFE_TO_BUY,
    PROCEED_WITH_CAUTION,
    DELAY_PURCHASE
}

data class PurchaseSimulationResult(
    val itemTitle: String,
    val simulatedAmount: BigDecimal,
    val decision: SimulationDecision,
    val currentSafeDaily: BigDecimal,
    val newSafeDaily: BigDecimal,
    val currentRemainingBudget: BigDecimal,
    val newRemainingBudget: BigDecimal,
    val budgetImpactPct: Int,
    val verdictTitle: String,
    val explanation: String
)

@Singleton
class PurchaseSimulatorUseCase @Inject constructor(
    private val safeToSpendEngine: SafeToSpendEngine
) {

    suspend operator fun invoke(
        profileId: String,
        itemTitle: String,
        amount: BigDecimal
    ): PurchaseSimulationResult {
        val safeResult = safeToSpendEngine.calculate(profileId)

        val cleanTitle = itemTitle.ifBlank { "Item" }
        val newRemaining = safeResult.remainingBudget.subtract(amount)
        val days = safeResult.daysRemainingInMonth

        val newNetAvailable = newRemaining.subtract(safeResult.upcomingRecurringCommitments)
        val newSafeDaily = if (newNetAvailable > BigDecimal.ZERO) {
            newNetAvailable.divide(BigDecimal(days), 2, RoundingMode.HALF_EVEN)
        } else {
            BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN)
        }

        val budgetImpactPct = if (safeResult.totalBudget > BigDecimal.ZERO) {
            amount.multiply(BigDecimal("100")).divide(safeResult.totalBudget, 0, RoundingMode.HALF_EVEN).toInt()
        } else 0

        val decision = when {
            newRemaining < BigDecimal.ZERO -> SimulationDecision.DELAY_PURCHASE
            newRemaining <= safeResult.totalBudget.multiply(BigDecimal("0.15")) -> SimulationDecision.PROCEED_WITH_CAUTION
            else -> SimulationDecision.SAFE_TO_BUY
        }

        val verdictTitle = when (decision) {
            SimulationDecision.SAFE_TO_BUY -> "Safe to Buy ✅"
            SimulationDecision.PROCEED_WITH_CAUTION -> "Proceed with Caution ⚠️"
            SimulationDecision.DELAY_PURCHASE -> "Delay Purchase Advised 🛑"
        }

        val explanation = when (decision) {
            SimulationDecision.SAFE_TO_BUY ->
                "Purchasing '$cleanTitle' for ₹$amount consumes $budgetImpactPct% of your budget, leaving ₹$newRemaining and a healthy ₹$newSafeDaily/day safe run-rate for the remaining $days days."
            SimulationDecision.PROCEED_WITH_CAUTION ->
                "Purchasing '$cleanTitle' for ₹$amount will tighten your remaining budget to ₹$newRemaining and reduce your daily safe spend from ₹${safeResult.safeDailySpend} to ₹$newSafeDaily/day."
            SimulationDecision.DELAY_PURCHASE ->
                "Purchasing '$cleanTitle' for ₹$amount will push you into a ₹${newRemaining.abs()} budget deficit for the month. Consider saving up or delaying until next month."
        }

        return PurchaseSimulationResult(
            itemTitle = cleanTitle,
            simulatedAmount = amount,
            decision = decision,
            currentSafeDaily = safeResult.safeDailySpend,
            newSafeDaily = newSafeDaily,
            currentRemainingBudget = safeResult.remainingBudget,
            newRemainingBudget = newRemaining,
            budgetImpactPct = budgetImpactPct,
            verdictTitle = verdictTitle,
            explanation = explanation
        )
    }
}
