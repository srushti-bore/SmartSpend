package com.smartspend.app.domain.model

import java.math.BigDecimal

data class CashFlowSummary(
    val totalIncome: BigDecimal = BigDecimal.ZERO,
    val totalExpense: BigDecimal = BigDecimal.ZERO,
    val netSavings: BigDecimal = BigDecimal.ZERO,
    val savingsRatePct: Double = 0.0,
    val incomeBySource: Map<IncomeSource, BigDecimal> = emptyMap(),
    val expenseByCategory: Map<String, BigDecimal> = emptyMap()
)
