package com.smartspend.app.domain.intelligence

import com.smartspend.app.domain.repository.ExpenseRepository
import com.smartspend.app.domain.repository.RecurringExpenseRepository
import kotlinx.coroutines.flow.first
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

enum class LeakSeverity {
    HIGH, MEDIUM, LOW
}

data class SpendingLeakItem(
    val title: String,
    val totalAmount: BigDecimal,
    val transactionCount: Int,
    val categoryName: String,
    val severity: LeakSeverity,
    val explanation: String,
    val suggestedAction: String
)

data class LeakHunterReport(
    val totalLeakedAmount: BigDecimal,
    val leaksFound: List<SpendingLeakItem>,
    val headline: String
)

@Singleton
class LeakHunterUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val recurringRepository: RecurringExpenseRepository
) {

    suspend operator fun invoke(profileId: String): LeakHunterReport {
        val calendar = Calendar.getInstance()
        val startCal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endCal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        val allExpenses = expenseRepository.getExpensesBetweenDates(profileId, startCal.timeInMillis, endCal.timeInMillis).first()
        val recurringList = recurringRepository.getActiveRecurringExpenses(profileId).first()

        val leaks = mutableListOf<SpendingLeakItem>()

        // 1. Detect Micro-Spending (Spends <= ₹250 grouped by merchant/title with count >= 4)
        val microSpends = allExpenses.filter { it.amount <= BigDecimal("250.00") }
        val microGrouped = microSpends.groupBy { it.title.trim().lowercase() }

        for ((title, items) in microGrouped) {
            if (items.size >= 4) {
                val totalMicro = items.map { it.amount }.fold(BigDecimal.ZERO) { acc, a -> acc.add(a) }
                leaks.add(
                    SpendingLeakItem(
                        title = items.first().title,
                        totalAmount = totalMicro,
                        transactionCount = items.size,
                        categoryName = "Micro-Spending",
                        severity = if (totalMicro > BigDecimal("1000")) LeakSeverity.HIGH else LeakSeverity.MEDIUM,
                        explanation = "${items.size} micro-transactions recorded this month, quietly adding up to ₹$totalMicro.",
                        suggestedAction = "Set a weekly cash limit for small casual purchases."
                    )
                )
            }
        }

        // 2. High-Frequency Merchant Spends (>= 5 transactions in same month)
        val merchantGrouped = allExpenses.groupBy { it.title.trim().lowercase() }
        for ((title, items) in merchantGrouped) {
            if (items.size >= 5 && microGrouped[title] == null) {
                val totalMerchant = items.map { it.amount }.fold(BigDecimal.ZERO) { acc, a -> acc.add(a) }
                leaks.add(
                    SpendingLeakItem(
                        title = items.first().title,
                        totalAmount = totalMerchant,
                        transactionCount = items.size,
                        categoryName = "Frequent Merchant",
                        severity = LeakSeverity.MEDIUM,
                        explanation = "Frequent visits (${items.size} times this month) totaled ₹$totalMerchant.",
                        suggestedAction = "Check if bulk purchasing or fewer trips can reduce overall spend."
                    )
                )
            }
        }

        // 3. Subscription Load
        for (sub in recurringList) {
            if (sub.amount >= BigDecimal("1000.00")) {
                leaks.add(
                    SpendingLeakItem(
                        title = sub.title,
                        totalAmount = sub.amount,
                        transactionCount = 1,
                        categoryName = "High-Cost Subscription",
                        severity = LeakSeverity.LOW,
                        explanation = "Fixed commitment of ₹${sub.amount} (${sub.frequency.name}).",
                        suggestedAction = "Evaluate if you are using this subscription actively."
                    )
                )
            }
        }

        val totalLeaked = leaks.map { it.totalAmount }.fold(BigDecimal.ZERO) { acc, a -> acc.add(a) }

        val headline = when {
            leaks.isEmpty() -> "No financial leaks detected! Your spending is tight and disciplined. 🎯"
            leaks.size == 1 -> "1 potential spending leak identified (₹$totalLeaked/mo)."
            else -> "${leaks.size} potential leaks identified totaling ₹$totalLeaked this month."
        }

        return LeakHunterReport(
            totalLeakedAmount = totalLeaked,
            leaksFound = leaks,
            headline = headline
        )
    }
}
