package com.smartspend.app.domain.usecase.export

import com.smartspend.app.domain.repository.CategoryRepository
import com.smartspend.app.domain.repository.ExpenseRepository
import com.smartspend.app.domain.repository.IncomeRepository
import com.smartspend.app.domain.repository.PaymentMethodRepository
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class ExportTransactionsUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository
) {
    suspend operator fun invoke(
        profileId: String,
        startDate: Long,
        endDate: Long
    ): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val categories = categoryRepository.getCategories(profileId).first().associateBy { it.id }
        val paymentMethods = paymentMethodRepository.getPaymentMethods(profileId).first().associateBy { it.id }

        val expenses = expenseRepository.getExpensesBetweenDates(profileId, startDate, endDate).first()
        val incomes = incomeRepository.getIncomesBetweenDates(profileId, startDate, endDate).first()

        val sb = StringBuilder()
        sb.append("Type,Date,Title,Category/Source,Amount,Currency,Payment Method,Notes\n")

        for (expense in expenses) {
            val dateStr = dateFormat.format(Date(expense.date))
            val catName = categories[expense.categoryId]?.name ?: "Uncategorized"
            val pmLabel = paymentMethods[expense.paymentMethodId]?.label ?: "Default"
            val safeTitle = escapeCsv(expense.title)
            val safeNotes = escapeCsv(expense.notes ?: "")

            sb.append("EXPENSE,$dateStr,$safeTitle,\"$catName\",${expense.amount},${expense.currency},\"$pmLabel\",$safeNotes\n")
        }

        for (income in incomes) {
            val dateStr = dateFormat.format(Date(income.date))
            val pmLabel = income.paymentMethodId?.let { paymentMethods[it]?.label } ?: "Default"
            val safeTitle = escapeCsv(income.title)
            val safeNotes = escapeCsv(income.notes ?: "")

            sb.append("INCOME,$dateStr,$safeTitle,\"${income.source.displayName}\",${income.amount},${income.currency},\"$pmLabel\",$safeNotes\n")
        }

        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }
}
