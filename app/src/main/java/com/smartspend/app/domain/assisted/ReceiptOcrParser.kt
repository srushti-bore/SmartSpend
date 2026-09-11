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

    // Tier 1: Definitive final payable total keywords (highest priority)
    private val tier1TotalKeywords = listOf(
        "grand total", "net payable", "total payable", "net amount",
        "amount payable", "total amount", "bill total", "final total",
        "final amount", "total due", "balance due", "total inr",
        "total rs", "total ₹", "total debit", "paid amount",
        "total paid", "total bill", "total charge", "net total"
    )

    // Tier 2: General total keywords
    private val tier2TotalKeywords = listOf(
        "total:", "total :", "total-", "total -", "total",
        "gross total", "sale total", "invoice total"
    )

    // Tier 3: Subtotals (only if no final total found)
    private val tier3TotalKeywords = listOf(
        "subtotal", "sub total", "sub-total"
    )

    // Negative keywords: numbers on these lines are NOT the bill total
    private val negativeKeywords = listOf(
        "cash tendered", "cash paid", "cash received", "tendered",
        "change return", "change returned", "change due", "change",
        "total savings", "you saved", "total discount", "discount total",
        "cgst", "sgst", "igst", "vat", "service tax", "cess",
        "total item", "total qty", "total quantity", "items count",
        "phone", "tel:", "mobile", "gstin", "fssai", "cin", "arn",
        "invoice no", "bill no", "order id", "table no", "token no",
        "pos no", "terminal", "round off", "rounded off"
    )

    // Noise header lines to discard during merchant extraction
    private val noiseHeaders = listOf(
        "tax invoice", "retail invoice", "cash memo", "bill of supply",
        "original for recipient", "duplicate for supplier", "triplicate",
        "payment receipt", "order summary", "e-receipt", "customer copy",
        "merchant copy", "welcome to", "welcome", "thank you", "thanks for visiting",
        "visit again", "have a nice day", "estimate", "quotation", "proforma invoice"
    )

    fun parse(ocrText: String, source: ExpenseSource = ExpenseSource.OCR): ParsedExpenseDraft {
        if (ocrText.isBlank()) {
            return ParsedExpenseDraft(source = source, rawInput = ocrText)
        }

        val lines = ocrText.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        // 1. Merchant Extraction
        val merchant = extractMerchant(lines)

        // 2. Amount Extraction
        val amount = extractTotalAmount(lines)

        // 3. Date Extraction
        val date = extractReceiptDate(lines)

        // 4. Payment Method Extraction
        val paymentType = extractPaymentType(ocrText)

        // 5. Category Suggestion
        val contextForCategory = (merchant + " " + ocrText.take(600)).trim()
        val suggestedCategory = categorySuggester.suggestCategoryName(contextForCategory)

        val cleanTitle = if (merchant.isNotBlank()) {
            merchant
        } else if (!suggestedCategory.isNullOrBlank()) {
            "$suggestedCategory Expense"
        } else {
            "Receipt Expense"
        }

        return ParsedExpenseDraft(
            title = cleanTitle,
            amount = amount,
            currency = "INR",
            date = date ?: System.currentTimeMillis(),
            suggestedCategoryName = suggestedCategory,
            suggestedPaymentType = paymentType,
            notes = if (merchant.isNotBlank()) "Scanned from $merchant receipt" else "Scanned from receipt",
            source = source,
            rawInput = ocrText,
            confidence = if (amount != null && merchant.isNotBlank()) 0.95f else if (amount != null) 0.8f else 0.5f
        )
    }

    /**
     * Extracts full merchant/business name from header lines without truncation.
     */
    private fun extractMerchant(lines: List<String>): String {
        val candidateLines = mutableListOf<String>()

        for (rawLine in lines.take(8)) {
            val line = rawLine.trim()
            val lower = line.lowercase()

            // Skip lines that are purely noise headers
            if (noiseHeaders.any { lower == it || lower.startsWith("$it ") || lower.endsWith(" $it") }) {
                continue
            }

            // Skip technical/administrative identifiers
            if (lower.startsWith("gstin") || lower.startsWith("fssai") || lower.startsWith("cin") ||
                lower.startsWith("pan:") || lower.startsWith("arn:") || lower.startsWith("bill no") ||
                lower.startsWith("inv no") || lower.startsWith("date:") || lower.startsWith("time:") ||
                lower.startsWith("tel:") || lower.startsWith("ph:") || lower.startsWith("phone:") ||
                lower.startsWith("order #") || lower.startsWith("table #")
            ) {
                continue
            }

            // Clean inline noise phrases from merchant line (e.g. "STARBUCKS - TAX INVOICE" -> "STARBUCKS")
            var cleanedLine = line
            for (noise in noiseHeaders) {
                cleanedLine = cleanedLine.replace(Regex("(?i)\\b$noise\\b"), "")
            }
            cleanedLine = cleanedLine.replace(Regex("""^[\s\-–—:]+|[\s\-–—:]+$"""), "").trim()

            // Discard lines with no letters or too short
            if (cleanedLine.length < 2 || !cleanedLine.any { it.isLetter() }) {
                continue
            }

            // Discard street addresses as standalone merchant name if already found candidate
            if (lower.startsWith("shop no") || lower.startsWith("plot no") || lower.startsWith("near ") || lower.startsWith("opp ")) {
                if (candidateLines.isNotEmpty()) break else continue
            }

            // Found a valid business title line
            candidateLines.add(cleanedLine)
            if (candidateLines.size >= 2) break
        }

        if (candidateLines.isEmpty()) return ""

        // Combine up to 2 header lines if line 2 looks like a continuation (e.g. "D-Mart" + "Supermarket")
        val rawMerchant = candidateLines.joinToString(" ")
        return toCleanTitleCase(rawMerchant)
    }

    /**
     * Extracts the exact net payable amount using prioritized keyword matching and next-line scanning.
     */
    private fun extractTotalAmount(lines: List<String>): BigDecimal? {
        val amountPattern = Regex(
            """(?:(?:₹|Rs\.?|INR|\$|€|£)\s*)?([0-9]+(?:[,\.][0-9]{2,3})*(?:[\.,][0-9]{1,2})?)\s*(?:/-|/=)?""",
            RegexOption.IGNORE_CASE
        )

        // Strategy 1: Check Tier 1 Keywords (Final Net Payable)
        val tier1Result = searchAmountForKeywords(lines, tier1TotalKeywords, amountPattern)
        if (tier1Result != null) return tier1Result

        // Strategy 2: Check Tier 2 Keywords (General Total)
        val tier2Result = searchAmountForKeywords(lines, tier2TotalKeywords, amountPattern)
        if (tier2Result != null) return tier2Result

        // Strategy 3: Check Tier 3 Keywords (Subtotal)
        val tier3Result = searchAmountForKeywords(lines, tier3TotalKeywords, amountPattern)
        if (tier3Result != null) return tier3Result

        // Strategy 4: Explicit Currency Symbol match (e.g. "₹ 1,450.00" or "Rs. 1450.00")
        val currencyTaggedPattern = Regex(
            """(?:₹|Rs\.?|INR)\s*([0-9]+(?:,[0-9]{2,3})*(?:\.[0-9]{1,2})?)\s*(?:/-|/=)?""",
            RegexOption.IGNORE_CASE
        )
        val taggedCandidates = mutableListOf<BigDecimal>()
        for (line in lines) {
            val lower = line.lowercase()
            if (negativeKeywords.any { lower.contains(it) }) continue
            for (match in currencyTaggedPattern.findAll(line)) {
                val numStr = cleanNumberString(match.groups[1]?.value ?: "")
                parseAmount(numStr)?.let {
                    if (it in BigDecimal("1.00")..BigDecimal("5000000.00")) {
                        taggedCandidates.add(it)
                    }
                }
            }
        }
        if (taggedCandidates.isNotEmpty()) {
            return taggedCandidates.maxOrNull()
        }

        // Strategy 5: Fallback scan all valid decimal amounts
        val generalCandidates = mutableListOf<BigDecimal>()
        for (line in lines) {
            val lower = line.lowercase()
            if (negativeKeywords.any { lower.contains(it) }) continue
            for (match in amountPattern.findAll(line)) {
                val numStr = cleanNumberString(match.groups[1]?.value ?: "")
                parseAmount(numStr)?.let {
                    // Ignore typical years (2024..2030) or phone numbers
                    if (it in BigDecimal("1.00")..BigDecimal("200000.00") &&
                        it != BigDecimal("2024.00") && it != BigDecimal("2025.00") && it != BigDecimal("2026.00") && it != BigDecimal("2027.00")
                    ) {
                        generalCandidates.add(it)
                    }
                }
            }
        }

        return generalCandidates.maxOrNull()
    }

    private fun searchAmountForKeywords(
        lines: List<String>,
        keywords: List<String>,
        amountPattern: Regex
    ): BigDecimal? {
        for (keyword in keywords) {
            // Search from bottom of the bill upwards (since totals are near the bottom)
            for (i in lines.indices.reversed()) {
                val line = lines[i]
                val lower = line.lowercase()

                if (lower.contains(keyword)) {
                    // Check if this line is disqualified by negative keywords (unless keyword explicitly overrides)
                    if (isDisqualifiedLine(lower, keyword)) continue

                    // 1. Try extracting amount from the same line
                    val sameLineMatches = amountPattern.findAll(line).toList()
                    if (sameLineMatches.isNotEmpty()) {
                        // Take the last number on the total line (usually total is on far right)
                        val lastMatch = sameLineMatches.last()
                        val numStr = cleanNumberString(lastMatch.groups[1]?.value ?: "")
                        val parsed = parseAmount(numStr)
                        if (parsed != null && parsed > BigDecimal.ZERO) {
                            return parsed
                        }
                    }

                    // 2. If no amount on the same line, check line immediately below (Line i + 1)
                    if (i + 1 < lines.size) {
                        val nextLine = lines[i + 1]
                        val nextLower = nextLine.lowercase()
                        if (!negativeKeywords.any { nextLower.contains(it) }) {
                            val nextLineMatch = amountPattern.findAll(nextLine).lastOrNull()
                            if (nextLineMatch != null) {
                                val numStr = cleanNumberString(nextLineMatch.groups[1]?.value ?: "")
                                val parsed = parseAmount(numStr)
                                if (parsed != null && parsed > BigDecimal.ZERO) {
                                    return parsed
                                }
                            }
                        }
                    }
                }
            }
        }
        return null
    }

    private fun isDisqualifiedLine(lineLower: String, matchedKeyword: String): Boolean {
        for (neg in negativeKeywords) {
            if (lineLower.contains(neg) && !matchedKeyword.contains(neg)) {
                return true
            }
        }
        return false
    }

    private fun cleanNumberString(raw: String): String {
        var s = raw.trim()
        // If comma is used as decimal separator e.g. "1450,50" -> "1450.50"
        if (s.contains(",") && !s.contains(".")) {
            val parts = s.split(",")
            if (parts.size == 2 && parts[1].length <= 2) {
                s = "${parts[0]}.${parts[1]}"
            }
        }
        return s.replace(",", "")
    }

    private fun parseAmount(numStr: String): BigDecimal? {
        if (numStr.isBlank()) return null
        return try {
            val bd = BigDecimal(numStr).setScale(2, RoundingMode.HALF_EVEN)
            if (bd > BigDecimal.ZERO && bd < BigDecimal(10000000)) bd else null
        } catch (_: Exception) {
            null
        }
    }

    private fun extractReceiptDate(lines: List<String>): Long? {
        val datePatterns = listOf(
            Regex("""\b(\d{1,2}[/-]\d{1,2}[/-]\d{2,4})\b""") to listOf("dd/MM/yyyy", "dd-MM-yyyy", "dd/MM/yy", "dd-MM-yy", "MM/dd/yyyy"),
            Regex("""\b(\d{1,2}\s+(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\s+\d{2,4})\b""", RegexOption.IGNORE_CASE) to listOf("dd MMM yyyy", "dd MMMM yyyy"),
            Regex("""\b((?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\s+\d{1,2},?\s+\d{2,4})\b""", RegexOption.IGNORE_CASE) to listOf("MMM dd, yyyy", "MMMM dd, yyyy", "MMM dd yyyy")
        )

        for (line in lines) {
            for ((regex, formats) in datePatterns) {
                val match = regex.find(line)
                if (match != null) {
                    val dateStr = match.value.replace(",", "")
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
            lower.contains("upi") || lower.contains("gpay") || lower.contains("phonepe") ||
            lower.contains("paytm") || lower.contains("bhim") || lower.contains("qr code") -> PaymentType.UPI
            lower.contains("visa") || lower.contains("mastercard") || lower.contains("credit card") ||
            lower.contains("amex") || lower.contains("diners") -> PaymentType.CREDIT_CARD
            lower.contains("debit card") || lower.contains("rupay") -> PaymentType.DEBIT_CARD
            lower.contains("cash") || lower.contains("cash paid") || lower.contains("change returned") -> PaymentType.CASH
            lower.contains("net banking") || lower.contains("neft") || lower.contains("rtgs") || lower.contains("imps") -> PaymentType.BANK_ACCOUNT
            lower.contains("wallet") || lower.contains("paytm wallet") || lower.contains("amazon pay") -> PaymentType.DIGITAL_WALLET
            else -> null
        }
    }

    private fun toCleanTitleCase(text: String): String {
        return text.split(Regex("""\s+"""))
            .filter { it.isNotBlank() }
            .joinToString(" ") { word ->
                val lower = word.lowercase()
                // Keep known acronyms or words like PVT, LTD, &
                if (lower == "pvt" || lower == "ltd" || lower == "llp" || lower == "inc" || lower == "co" || lower == "&") {
                    word.uppercase()
                } else {
                    word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                }
            }
    }
}
