package com.smartspend.app.domain.usecase.recurring

import com.smartspend.app.domain.model.Expense
import com.smartspend.app.domain.model.ExpenseSource
import com.smartspend.app.domain.model.RecurringFrequency
import com.smartspend.app.domain.repository.ExpenseRepository
import com.smartspend.app.domain.repository.RecurringExpenseRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject

class ProcessDueRecurringExpensesUseCase @Inject constructor(
    private val recurringExpenseRepository: RecurringExpenseRepository,
    private val expenseRepository: ExpenseRepository
) {
    suspend operator fun invoke(cutoffTimeMs: Long = System.currentTimeMillis()): Int {
        val dueItems = recurringExpenseRepository.getDueRecurringExpenses(cutoffTimeMs)
        var processedCount = 0

        for (item in dueItems) {
            if (item.autoCreate) {
                val expense = Expense(
                    id = UUID.randomUUID().toString(),
                    profileId = item.profileId,
                    title = item.title,
                    amount = item.amount,
                    currency = item.currency,
                    categoryId = item.categoryId,
                    paymentMethodId = item.paymentMethodId,
                    date = item.nextDueDate,
                    notes = item.notes ?: "Auto-generated from subscription",
                    isRecurring = true,
                    source = ExpenseSource.MANUAL,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                expenseRepository.addExpense(expense)
            }

            // Calculate next due date
            val nextDueLocal = Instant.ofEpochMilli(item.nextDueDate)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            val advancedLocalDate = when (item.frequency) {
                RecurringFrequency.DAILY -> nextDueLocal.plusDays(1)
                RecurringFrequency.WEEKLY -> nextDueLocal.plusWeeks(1)
                RecurringFrequency.MONTHLY -> nextDueLocal.plusMonths(1)
                RecurringFrequency.YEARLY -> nextDueLocal.plusYears(1)
            }

            val nextDueMs = advancedLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val updated = item.copy(
                nextDueDate = nextDueMs,
                lastGeneratedDate = item.nextDueDate,
                updatedAt = System.currentTimeMillis()
            )
            recurringExpenseRepository.updateRecurringExpense(updated)
            processedCount++
        }

        return processedCount
    }
}
