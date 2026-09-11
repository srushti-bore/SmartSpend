package com.smartspend.app.domain.usecase.expense

import com.smartspend.app.domain.model.Expense
import com.smartspend.app.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.first
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class DuplicateCheckResult(
    val isDuplicate: Boolean = false,
    val matchedExpense: Expense? = null,
    val warningMessage: String? = null
)

class DuplicateGuardUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {

    suspend operator fun invoke(
        profileId: String,
        title: String,
        amount: BigDecimal?,
        date: Long,
        excludeExpenseId: String? = null
    ): DuplicateCheckResult {
        if (amount == null || amount <= BigDecimal.ZERO || title.isBlank()) {
            return DuplicateCheckResult()
        }

        // Search within ±3 days window
        val windowMs = TimeUnit.DAYS.toMillis(3)
        val startDate = date - windowMs
        val endDate = date + windowMs

        val candidateExpenses = expenseRepository.getExpensesBetweenDates(profileId, startDate, endDate).first()

        val normalizedTitle = title.trim().lowercase()

        for (candidate in candidateExpenses) {
            if (candidate.id == excludeExpenseId) continue

            // 1. Exact amount match
            if (candidate.amount.compareTo(amount) == 0) {
                val candidateNormalizedTitle = candidate.title.trim().lowercase()

                // Check title similarity (exact, containment, or Levenshtein similarity)
                val similarity = calculateSimilarity(normalizedTitle, candidateNormalizedTitle)
                if (similarity >= 0.7f || normalizedTitle.contains(candidateNormalizedTitle) || candidateNormalizedTitle.contains(normalizedTitle)) {
                    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    val formattedDate = sdf.format(Date(candidate.date))
                    val message = "Potential duplicate detected: '${candidate.title}' for ₹${candidate.amount} on $formattedDate"
                    return DuplicateCheckResult(
                        isDuplicate = true,
                        matchedExpense = candidate,
                        warningMessage = message
                    )
                }
            }
        }

        return DuplicateCheckResult(isDuplicate = false)
    }

    private fun calculateSimilarity(s1: String, s2: String): Float {
        if (s1 == s2) return 1.0f
        if (s1.isEmpty() || s2.isEmpty()) return 0.0f

        val maxLen = maxOf(s1.length, s2.length)
        val distance = levenshteinDistance(s1, s2)
        return 1.0f - (distance.toFloat() / maxLen)
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }

        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j

        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1,      // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }
        return dp[s1.length][s2.length]
    }
}
