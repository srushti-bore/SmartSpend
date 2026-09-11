package com.smartspend.app.feature.income

import com.smartspend.app.core.common.Resource
import com.smartspend.app.domain.model.CashFlowSummary
import com.smartspend.app.domain.model.Income
import com.smartspend.app.domain.model.IncomeSource
import java.math.BigDecimal

data class IncomeUiState(
    val incomeResource: Resource<List<Income>> = Resource.Loading,
    val cashFlowResource: Resource<CashFlowSummary> = Resource.Loading,
    val selectedSourceFilter: IncomeSource? = null,
    val isAddIncomeDialogOpen: Boolean = false,
    val isSubmitting: Boolean = false,
    val userErrorMessage: String? = null
)
