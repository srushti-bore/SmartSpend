package com.smartspend.app.feature.account

import com.smartspend.app.core.common.Resource
import com.smartspend.app.domain.model.Account

data class AccountUiState(
    val accountsResource: Resource<List<Account>> = Resource.Loading,
    val isAddDialogOpen: Boolean = false,
    val isSubmitting: Boolean = false,
    val userErrorMessage: String? = null
)
