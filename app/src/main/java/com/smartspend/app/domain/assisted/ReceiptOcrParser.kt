package com.smartspend.app.domain.assisted

import com.smartspend.app.domain.model.ExpenseSource
import com.smartspend.app.domain.model.PaymentType
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReceiptOcrParser @Inject constructor(
    private val categorySuggester: SmartCategorySuggester
) {

    private val totalKeywords = listOf(
        "grand total", "net payable", "total amount", "amount paid",
        "final total", "total bill", "total", "net amount", "paid",
        "subtotal", "sub total"
    )

    private val noiseHeaders = listOf(
        "tax invoice", "retail invoice", "cash memo", "bill of supply",
        "receipt", "welcome", "gstin", "original for recipient",
        "duplicate for supplier", "triplicate", "invoice", "payment receipt",
        "order summary", "e-receipt", "customer copy", "merchant copy"
    )

    fun parse(ocrText: String, source: ExpenseSource = ExpenseSource.OCR): ParsedExpenseDraft {
        if (ocrText.isBlank()) {
            return ParsedExpenseDraft(source = source, rawInput = ocrText)
        }

        val lines = ocrText.lines().map { it.trim() }.filter { it.isNotBlank() }

        // 1. Merchant Extraction
        val merchant = extractMerchant(lines)

        // 2. Amount Extraction
        val amount = extractTotalAmount(lines)

        // 3. Date Extraction
        val date = extractReceiptDate(lines)

        // 4. Payment Method Extraction
        val paymentType = extractPaymentType(ocrText)

        // 5. Category Suggestion
        val suggestedCategory = categorySuggester.suggestCategoryName(merchant + " " + ocrText.take(500))

        return ParsedExpenseDraft(
            title = merchant.ifBlank { suggestedCategory ?: "Receipt Expense" },
            amount = amount,
            currency = "INR",
            date = date ?: System.currentTimeMillis(),
            suggestedCategoryName = suggestedCategory,
            suggestedPaymentType = paymentType,
            notes = "Scanned from receipt",
            source = source,
            rawInput = ocrText,
            confidence = if (amount != null && merchant.isNotBlank()) 0.9f else 0.6f
        )
    }

    private fun extractMerchant(lines: List<String>): String {
        for (line in lines.take(6)) {
            val lower = line.lowercase()
            // Skip noisy headers
            if (noiseHeaders.any { lower.contains(it) }) continue
            if (lower.startsWith("date:") || lower.startsWith("time:") || lower.startsWith("tel:") || lower.startsWith("ph:")) continue
            if (lower.contains("gstin") || lower.contains("fssai") || lower.contains("cin:")) continue
            if (line.length in 3..40 && line.any { it.isLetter() }) {
                return line.replace(Regex("""[^a-zA-Z0-9\s&'-]"""), "").trim()
            }
        }
        return ""
    }

    private fun extractTotalAmount(lines: List<String>): BigDecimal? {
        val amountPattern = Regex("""(?:(?:₹|Rs\.?|INR|\$|€)\s*)?([0-9]+(?:,[0-9]{2,3})*(?:\.[0-9]{1,2})?)\s*(?:/-)?""", RegexOption.IGNORE_CASE)

        // Phase A: Search lines containing total keywords (starting from bottom up)
        for (keyword in totalKeywords) {
            for (line in lines.reversed()) {
                val lower = line.lowercase()
                if (lower.contains(keyword)) {
                    val match = amountPattern.findAll(line).lastOrNull()
                    if (match != null) {
                        val numStr = match.groups[1]?.value?.replace(",", "") ?: continue
                        try {
                            val parsed = BigDecimal(numStr).setScale(2, RoundingMode.HALF_EVEN)
                            if (parsed > BigDecimal.ZERO) return parsed
                        } catch (_: Exception) {}
                    }
                }
            }
        }

        // Phase B: Scan all numbers and pick the most probable highest valid total (ignoring extreme outliers like phone/GST numbers)
        val candidateAmounts = mutableListOf<BigDecimal>()
        for (line in lines) {
            // Ignore phone numbers or GST lines
            val lower = line.lowercase()
            if (lower.contains("ph") || lower.contains("tel") || lower.contains("gstin") || lower.contains("fssai")) continue

            for (match in amountPattern.findAll(line)) {
                val numStr = match.groups[1]?.value?.replace(",", "") ?: continue
                try {
                    val parsed = BigDecimal(numStr).setScale(2, RoundingMode.HALF_EVEN)
                    // Avoid 10-digit phone numbers or years like 2026
                    if (parsed > BigDecimal.ZERO && parsed < BigDecimal(5000000) && parsed != BigDecimal("2024.00") && parsed != BigDecimal("2025.00") && parsed != BigDecimal("2026.00")) {
                        candidateAmounts.add(parsed)
                    }
                } catch (_: Exception) {}
            }
        }

        return candidateAmounts.maxOrNull()
    }

    private fun extractReceiptDate(lines: List<String>): Long? {
        val datePatterns = listOf(
            Regex("""\b(\d{1,2}[/-]\d{1,2}[/-]\d{2,4})\b""") to listOf("dd/MM/yyyy", "dd-MM-yyyy", "dd/MM/yy", "dd-MM-yy", "MM/dd/yyyy"),
            Regex("""\b(\d{1,2}\s+(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\s+\d{2,4})\b""", RegexOption.IGNORE_CASE) to listOf("dd MMM yyyy", "dd MMMM yyyy")
        )

        for (line in lines) {
            for ((regex, formats) in datePatterns) {
                val match = regex.find(line)
                if (match != null) {
                    val dateStr = match.value
                    for (format in formats) {
                        try {
                            val sdf = SimpleDateFormat(format, Locale.ROOT)
                            sdf.isLenient = false
                            val parsed = sdf.parse(dateStr)
                            if (parsed != null && parsed.time <= System.currentTimeMillis() + 86400000L) {
                                return parsed.time
                            }
                        } catch (_: Exception) {}
                    }
                }
            }
        }
        return null
    }

    private fun extractPaymentType(ocrText: String): PaymentType? {
        val lower = ocrText.lowercase()
        return when {
            lower.contains("upi") || lower.contains("gpay") || lower.contains("phonepe") || lower.contains("paytm") || lower.contains("bhim") -> PaymentType.UPI
            lower.contains("visa") || lower.contains("mastercard") || lower.contains("credit card") || lower.contains("amex") -> PaymentType.CREDIT_CARD
            lower.contains("debit card") || lower.contains("rupay") -> PaymentType.DEBIT_CARD
            lower.contains("cash") || lower.contains("change returned") -> PaymentType.CASH
            else -> null
        }
    }
}
