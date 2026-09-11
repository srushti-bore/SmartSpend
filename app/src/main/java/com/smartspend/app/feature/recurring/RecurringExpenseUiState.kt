package com.smartspend.app.feature.recurring

import com.smartspend.app.core.common.Resource
import com.smartspend.app.domain.model.Category
import com.smartspend.app.domain.model.PaymentMethod
import com.smartspend.app.domain.model.RecurringExpense
import java.math.BigDecimal

data class RecurringExpenseUiState(
    val recurringResource: Resource<List<RecurringExpense>> = Resource.Loading,
    val monthlyCommitment: BigDecimal = BigDecimal.ZERO,
    val categories: List<Category> = emptyList(),
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val isAddDialogOpen: Boolean = false,
    val isSubmitting: Boolean = false,
    val userErrorMessage: String? = null
)
