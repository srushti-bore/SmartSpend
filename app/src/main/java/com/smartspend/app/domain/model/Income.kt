package com.smartspend.app.domain.model

import java.math.BigDecimal

enum class IncomeSource(val displayName: String) {
    SALARY("Salary"),
    FREELANCE("Freelance"),
    BUSINESS("Business"),
    INVESTMENT("Investment"),
    RENTAL("Rental"),
    POCKET_MONEY("Pocket Money"),
    OTHER("Other")
}

data class Income(
    val id: String,
    val profileId: String,
    val source: IncomeSource,
    val title: String,
    val amount: BigDecimal,
    val currency: String = "INR",
    val date: Long, // Epoch ms
    val paymentMethodId: String? = null,
    val notes: String? = null,
    val isRecurring: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
