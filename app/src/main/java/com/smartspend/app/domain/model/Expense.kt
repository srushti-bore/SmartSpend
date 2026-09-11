package com.smartspend.app.domain.model

import java.math.BigDecimal

enum class ExpenseSource {
    MANUAL,
    VOICE,
    OCR,
    QUICK_ADD,
    SCREENSHOT,
    IMPORT
}

data class Expense(
    val id: String,
    val profileId: String,
    val title: String,
    val amount: BigDecimal,
    val currency: String = "INR",
    val categoryId: String,
    val paymentMethodId: String,
    val date: Long, // Epoch timestamp in ms
    val notes: String? = null,
    val isRecurring: Boolean = false,
    val source: ExpenseSource = ExpenseSource.MANUAL,
    val attachmentRef: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
