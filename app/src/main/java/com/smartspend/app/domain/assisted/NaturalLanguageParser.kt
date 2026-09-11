package com.smartspend.app.domain.assisted

import com.smartspend.app.domain.model.ExpenseSource
import com.smartspend.app.domain.model.PaymentType
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NaturalLanguageParser @Inject constructor(
    private val categorySuggester: SmartCategorySuggester
) {

    private val amountRegex = Regex("""(?:(?:₹|Rs\.?|INR|\$|€)\s*)?([0-9]+(?:,[0-9]{2,3})*(?:\.[0-9]{1,2})?)\s*(?:/-)?(?:\s*(?:₹|Rs\.?|INR|\$|€))?""", RegexOption.IGNORE_CASE)

    fun parse(input: String, source: ExpenseSource = ExpenseSource.QUICK_ADD): ParsedExpenseDraft {
        if (input.isBlank()) {
            return ParsedExpenseDraft(source = source, rawInput = input)
        }

        var workingText = input.trim()

        // 1. Extract Amount
        var extractedAmount: BigDecimal? = null
        val amountMatches = amountRegex.findAll(workingText).toList()
        for (match in amountMatches) {
            val rawNum = match.groups[1]?.value?.replace(",", "") ?: continue
            try {
                val parsed = BigDecimal(rawNum).setScale(2, RoundingMode.HALF_EVEN)
                if (parsed > BigDecimal.ZERO) {
                    extractedAmount = parsed
                    // Remove amount substring from working text
                    workingText = workingText.removeRange(match.range).trim()
                    break
                }
            } catch (_: Exception) {
                // Ignore parse errors on number
            }
        }

        // 2. Extract Date
        val (extractedDate, textAfterDate) = extractDate(workingText)
        workingText = textAfterDate

        // 3. Extract Payment Method
        val (extractedPaymentType, textAfterPayment) = extractPaymentMethod(workingText)
        workingText = textAfterPayment

        // 4. Suggest Category
        val suggestedCategory = categorySuggester.suggestCategoryName(input)

        // 5. Clean Title & Notes
        val (title, notes) = extractTitleAndNotes(workingText)

        val finalTitle = if (title.isNotBlank()) title else (suggestedCategory ?: "Expense")

        return ParsedExpenseDraft(
            title = finalTitle.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
            amount = extractedAmount,
            currency = "INR",
            date = extractedDate,
            suggestedCategoryName = suggestedCategory,
            suggestedPaymentType = extractedPaymentType,
            notes = notes,
            source = source,
            rawInput = input,
            confidence = if (extractedAmount != null && title.isNotBlank()) 0.95f else 0.7f
        )
    }

    private fun extractDate(text: String): Pair<Long, String> {
        val calendar = Calendar.getInstance()
        var updatedText = text

        val dateKeywords = listOf(
            Regex("""\b(day before yesterday|parso)\b""", RegexOption.IGNORE_CASE) to -2,
            Regex("""\b(yesterday|kal|last night)\b""", RegexOption.IGNORE_CASE) to -1,
            Regex("""\b(today|aaj)\b""", RegexOption.IGNORE_CASE) to 0
        )

        for ((regex, dayOffset) in dateKeywords) {
            val match = regex.find(updatedText)
            if (match != null) {
                calendar.add(Calendar.DAY_OF_YEAR, dayOffset)
                calendar.set(Calendar.HOUR_OF_DAY, 12)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                updatedText = updatedText.removeRange(match.range).trim()
                return Pair(calendar.timeInMillis, updatedText)
            }
        }

        // Days of the week (e.g. "last monday", "on tuesday", "wednesday")
        val daysOfWeek = mapOf(
            "sunday" to Calendar.SUNDAY,
            "monday" to Calendar.MONDAY,
            "tuesday" to Calendar.TUESDAY,
            "wednesday" to Calendar.WEDNESDAY,
            "thursday" to Calendar.THURSDAY,
            "friday" to Calendar.FRIDAY,
            "saturday" to Calendar.SATURDAY
        )

        for ((dayName, dayConstant) in daysOfWeek) {
            val dayRegex = Regex("""\b(?:on|last)?\s*$dayName\b""", RegexOption.IGNORE_CASE)
            val match = dayRegex.find(updatedText)
            if (match != null) {
                val currentDay = calendar.get(Calendar.DAY_OF_WEEK)
                var diff = currentDay - dayConstant
                if (diff <= 0) diff += 7
                calendar.add(Calendar.DAY_OF_YEAR, -diff)
                calendar.set(Calendar.HOUR_OF_DAY, 12)
                calendar.set(Calendar.MINUTE, 0)
                updatedText = updatedText.removeRange(match.range).trim()
                return Pair(calendar.timeInMillis, updatedText)
            }
        }

        // Explicit standard date formats (e.g., 2026-09-10, 10/09/2026, 10-09-2026)
        val isoDateRegex = Regex("""\b(\d{4}[-/]\d{1,2}[-/]\d{1,2})\b""")
        val isoMatch = isoDateRegex.find(updatedText)
        if (isoMatch != null) {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
                val parsed = sdf.parse(isoMatch.value.replace("/", "-"))
                if (parsed != null) {
                    updatedText = updatedText.removeRange(isoMatch.range).trim()
                    return Pair(parsed.time, updatedText)
                }
            } catch (_: Exception) {}
        }

        return Pair(System.currentTimeMillis(), updatedText)
    }

    private fun extractPaymentMethod(text: String): Pair<PaymentType?, String> {
        var updatedText = text

        val paymentKeywords = listOf(
            PaymentType.UPI to listOf("upi", "gpay", "google pay", "phonepe", "paytm", "bhim", "cred"),
            PaymentType.CASH to listOf("cash", "rokh"),
            PaymentType.CREDIT_CARD to listOf("credit card", "credit", "cc"),
            PaymentType.DEBIT_CARD to listOf("debit card", "debit", "dc", "card"),
            PaymentType.BANK_ACCOUNT to listOf("bank transfer", "net banking", "neft", "imps", "rtgs", "bank"),
            PaymentType.DIGITAL_WALLET to listOf("wallet", "amazon pay", "mobikwik")
        )

        for ((type, keywords) in paymentKeywords) {
            for (keyword in keywords) {
                val regex = Regex("""\b(?:by|via|with|paid through|in|through)?\s*${Regex.escape(keyword)}\b""", RegexOption.IGNORE_CASE)
                val match = regex.find(updatedText)
                if (match != null) {
                    updatedText = updatedText.removeRange(match.range).trim()
                    return Pair(type, updatedText)
                }
            }
        }

        return Pair(null, updatedText)
    }

    private fun extractTitleAndNotes(text: String): Pair<String, String?> {
        var cleaned = text.trim()

        // Clean common noise words and prepositions
        cleaned = cleaned.replace(Regex("""^(?:paid|bought|spent|for|at|to)\s+""", RegexOption.IGNORE_CASE), "")
        cleaned = cleaned.replace(Regex("""\s+(?:for|at|to)\s*$""", RegexOption.IGNORE_CASE), "")
        cleaned = cleaned.replace(Regex("""\s{2,}"""), " ")

        // Check for "with <notes>" or "for <notes>"
        val notesRegex = Regex("""\b(?:for|with|note:|notes:)\s+(.+)$""", RegexOption.IGNORE_CASE)
        val notesMatch = notesRegex.find(cleaned)

        var notes: String? = null
        var title = cleaned

        if (notesMatch != null) {
            val potentialNotes = notesMatch.groups[1]?.value?.trim()
            val prefix = cleaned.substring(0, notesMatch.range.first).trim()
            if (prefix.isNotBlank() && !potentialNotes.isNullOrBlank()) {
                title = prefix
                notes = potentialNotes
            }
        }

        // Clean extra punctuation
        title = title.replace(Regex("""^[,\-.:;]+|[,\-.:;]+$"""), "").trim()

        return Pair(title, notes)
    }
}
