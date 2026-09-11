package com.smartspend.app.domain.model

import java.math.BigDecimal

enum class RecurringFrequency(val displayName: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly")
}

data class RecurringExpense(
    val id: String,
    val profileId: String,
    val title: String,
    val amount: BigDecimal,
    val currency: String = "INR",
    val categoryId: String,
    val paymentMethodId: String,
    val frequency: RecurringFrequency,
    val startDate: Long,
    val nextDueDate: Long,
    val lastGeneratedDate: Long? = null,
    val isActive: Boolean = true,
    val autoCreate: Boolean = true,
    val notifyBeforeDays: Int = 1,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
