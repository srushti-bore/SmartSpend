package com.smartspend.app.domain.assisted

import com.smartspend.app.domain.model.ExpenseSource
import com.smartspend.app.domain.model.PaymentType
import java.math.BigDecimal

data class ParsedExpenseDraft(
    val title: String = "",
    val amount: BigDecimal? = null,
    val currency: String = "INR",
    val date: Long = System.currentTimeMillis(),
    val suggestedCategoryName: String? = null,
    val suggestedPaymentType: PaymentType? = null,
    val notes: String? = null,
    val source: ExpenseSource = ExpenseSource.QUICK_ADD,
    val rawInput: String = "",
    val confidence: Float = 1.0f
)
