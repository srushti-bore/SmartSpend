package com.smartspend.app.domain.usecase.income

import com.smartspend.app.domain.model.CashFlowSummary
import com.smartspend.app.domain.repository.ExpenseRepository
import com.smartspend.app.domain.repository.IncomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class GetCashFlowSummaryUseCase @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository
) {
    operator fun invoke(profileId: String, startDate: Long, endDate: Long): Flow<CashFlowSummary> {
        val totalIncomeFlow = incomeRepository.getTotalIncome(profileId, startDate, endDate)
        val totalExpenseFlow = expenseRepository.getTotalSpending(profileId, startDate, endDate)
        val incomeAggregatesFlow = incomeRepository.getIncomeSourceAggregates(profileId, startDate, endDate)
        val expenseAggregatesFlow = expenseRepository.getCategorySpending(profileId, startDate, endDate)

        return combine(
            totalIncomeFlow,
            totalExpenseFlow,
            incomeAggregatesFlow,
            expenseAggregatesFlow
        ) { totalIncome, totalExpense, incomeAggs, expenseAggs ->
            val netSavings = totalIncome.subtract(totalExpense)
            val savingsRate = if (totalIncome > BigDecimal.ZERO) {
                netSavings.divide(totalIncome, 4, RoundingMode.HALF_EVEN)
                    .multiply(BigDecimal("100"))
                    .toDouble()
                    .coerceIn(-100.0, 100.0)
            } else {
                0.0
            }

            val incomeBySource = incomeAggs.associate { it.source to it.totalAmount }
            val expenseByCategory = expenseAggs.associate { it.categoryName to it.totalAmount }

            CashFlowSummary(
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                netSavings = netSavings,
                savingsRatePct = savingsRate,
                incomeBySource = incomeBySource,
                expenseByCategory = expenseByCategory
            )
        }
    }
}
