package com.smartspend.app.domain.usecase.export

import com.smartspend.app.domain.assisted.SmartCategorySuggester
import com.smartspend.app.domain.model.Category
import com.smartspend.app.domain.model.Expense
import com.smartspend.app.domain.model.ExpenseSource
import com.smartspend.app.domain.model.PaymentMethod
import com.smartspend.app.domain.repository.ExpenseRepository
import com.smartspend.app.domain.usecase.expense.DuplicateGuardUseCase
import kotlinx.coroutines.flow.first
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

data class ImportCandidateExpense(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: BigDecimal,
    val currency: String = "INR",
    val date: Long,
    val rawCategory: String? = null,
    val suggestedCategory: Category? = null,
    val rawPaymentMethod: String? = null,
    val suggestedPaymentMethod: PaymentMethod? = null,
    val notes: String? = null,
    val isDuplicate: Boolean = false,
    val duplicateWarning: String? = null,
    val isSelected: Boolean = true
)

data class CsvParseResult(
    val totalRows: Int,
    val validCandidates: List<ImportCandidateExpense>,
    val errorRowsCount: Int
)

class CsvImportUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val categorySuggester: SmartCategorySuggester,
    private val duplicateGuard: DuplicateGuardUseCase
) {

    suspend fun parseCsv(
        inputStream: InputStream,
        profileId: String,
        categories: List<Category>,
        paymentMethods: List<PaymentMethod>
    ): CsvParseResult {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val lines = reader.readLines().filter { it.isNotBlank() }

        if (lines.isEmpty()) {
            return CsvParseResult(0, emptyList(), 0)
        }

        val delimiter = detectDelimiter(lines.first())
        val headers = splitCsvRow(lines.first(), delimiter).map { it.trim().lowercase() }

        // Find column indices
        var dateIdx = headers.indexOfFirst { it.contains("date") || it.contains("time") }
        var titleIdx = headers.indexOfFirst { it.contains("title") || it.contains("merchant") || it.contains("description") || it.contains("narration") || it.contains("details") || it.contains("item") }
        var amountIdx = headers.indexOfFirst { it.contains("amount") || it.contains("debit") || it.contains("spent") || it.contains("cost") || it.contains("price") || it.contains("val") }
        var categoryIdx = headers.indexOfFirst { it.contains("category") || it.contains("group") }
        var paymentIdx = headers.indexOfFirst { it.contains("payment") || it.contains("mode") || it.contains("method") || it.contains("account") }
        var notesIdx = headers.indexOfFirst { it.contains("note") || it.contains("remark") || it.contains("comment") }

        // Fallbacks if header matching fails
        if (dateIdx == -1) dateIdx = 0
        if (titleIdx == -1) titleIdx = if (headers.size > 1) 1 else 0
        if (amountIdx == -1) amountIdx = if (headers.size > 2) 2 else 1

        val candidates = mutableListOf<ImportCandidateExpense>()
        var errorCount = 0

        for (line in lines.drop(1)) {
            val columns = splitCsvRow(line, delimiter)
            if (columns.size <= maxOf(titleIdx, amountIdx)) {
                errorCount++
                continue
            }

            try {
                val rawTitle = columns.getOrNull(titleIdx)?.trim() ?: ""
                val rawAmountStr = columns.getOrNull(amountIdx)?.trim()?.replace("\"", "")?.replace("₹", "")?.replace("Rs.", "")?.replace(",", "")?.trim() ?: ""
                val rawDateStr = columns.getOrNull(dateIdx)?.trim() ?: ""
                val rawCategory = if (categoryIdx >= 0) columns.getOrNull(categoryIdx)?.trim() else null
                val rawPayment = if (paymentIdx >= 0) columns.getOrNull(paymentIdx)?.trim() else null
                val rawNotes = if (notesIdx >= 0) columns.getOrNull(notesIdx)?.trim() else null

                val amount = BigDecimal(rawAmountStr).setScale(2, RoundingMode.HALF_EVEN)
                if (amount <= BigDecimal.ZERO || rawTitle.isBlank()) {
                    errorCount++
                    continue
                }

                val date = parseFlexibleDate(rawDateStr)

                // Suggest Category
                val matchedCategory = if (!rawCategory.isNullOrBlank()) {
                    categories.find { it.name.equals(rawCategory, ignoreCase = true) }
                        ?: categorySuggester.suggestCategory(rawCategory, categories)
                        ?: categorySuggester.suggestCategory(rawTitle, categories)
                } else {
                    categorySuggester.suggestCategory(rawTitle, categories)
                } ?: categories.firstOrNull()

                // Suggest Payment Method
                val matchedPaymentMethod = if (!rawPayment.isNullOrBlank()) {
                    paymentMethods.find { it.label.equals(rawPayment, ignoreCase = true) || it.type.name.equals(rawPayment, ignoreCase = true) }
                } else {
                    null
                } ?: paymentMethods.firstOrNull()

                // Run Duplicate Guard
                val duplicateResult = duplicateGuard(
                    profileId = profileId,
                    title = rawTitle,
                    amount = amount,
                    date = date
                )

                candidates.add(
                    ImportCandidateExpense(
                        title = rawTitle,
                        amount = amount,
                        date = date,
                        rawCategory = rawCategory,
                        suggestedCategory = matchedCategory,
                        rawPaymentMethod = rawPayment,
                        suggestedPaymentMethod = matchedPaymentMethod,
                        notes = rawNotes,
                        isDuplicate = duplicateResult.isDuplicate,
                        duplicateWarning = duplicateResult.warningMessage,
                        isSelected = !duplicateResult.isDuplicate // Auto-uncheck duplicates
                    )
                )
            } catch (_: Exception) {
                errorCount++
            }
        }

        return CsvParseResult(
            totalRows = lines.size - 1,
            validCandidates = candidates,
            errorRowsCount = errorCount
        )
    }

    suspend fun commitImport(
        profileId: String,
        selectedCandidates: List<ImportCandidateExpense>
    ): Int {
        var importedCount = 0
        for (candidate in selectedCandidates.filter { it.isSelected }) {
            val categoryId = candidate.suggestedCategory?.id ?: "cat_other_$profileId"
            val paymentMethodId = candidate.suggestedPaymentMethod?.id ?: "pm_cash_$profileId"

            val expense = Expense(
                id = candidate.id,
                profileId = profileId,
                title = candidate.title,
                amount = candidate.amount,
                currency = candidate.currency,
                categoryId = categoryId,
                paymentMethodId = paymentMethodId,
                date = candidate.date,
                notes = candidate.notes,
                isRecurring = false,
                source = ExpenseSource.IMPORT,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            expenseRepository.addExpense(expense)
            importedCount++
        }
        return importedCount
    }

    private fun detectDelimiter(headerLine: String): Char {
        return when {
            headerLine.count { it == ',' } >= headerLine.count { it == ';' } && headerLine.count { it == ',' } >= headerLine.count { it == '\t' } -> ','
            headerLine.count { it == ';' } > headerLine.count { it == '\t' } -> ';'
            else -> '\t'
        }
    }

    private fun splitCsvRow(line: String, delimiter: Char): List<String> {
        val result = mutableListOf<String>()
        val sb = java.lang.StringBuilder()
        var inQuotes = false

        for (ch in line) {
            when {
                ch == '\"' -> inQuotes = !inQuotes
                ch == delimiter && !inQuotes -> {
                    result.add(sb.toString())
                    sb.setLength(0)
                }
                else -> sb.append(ch)
            }
        }
        result.add(sb.toString())
        return result
    }

    private fun parseFlexibleDate(dateStr: String): Long {
        if (dateStr.isBlank()) return System.currentTimeMillis()

        val formats = listOf(
            "yyyy-MM-dd", "dd/MM/yyyy", "dd-MM-yyyy", "MM/dd/yyyy",
            "yyyy/MM/dd", "dd MMM yyyy", "dd-MMM-yyyy", "yyyy-MM-dd HH:mm:ss"
        )

        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.ROOT)
                val parsed = sdf.parse(dateStr.trim())
                if (parsed != null) return parsed.time
            } catch (_: Exception) {}
        }

        return System.currentTimeMillis()
    }
}
