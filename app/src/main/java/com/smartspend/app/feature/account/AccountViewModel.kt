package com.smartspend.app.feature.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartspend.app.core.common.Resource
import com.smartspend.app.core.datastore.PreferencesManager
import com.smartspend.app.domain.model.Account
import com.smartspend.app.domain.model.AccountType
import com.smartspend.app.domain.usecase.account.CreateAccountUseCase
import com.smartspend.app.domain.usecase.account.DeleteAccountUseCase
import com.smartspend.app.domain.usecase.account.GetAccountsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val getAccountsUseCase: GetAccountsUseCase,
    private val createAccountUseCase: CreateAccountUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    private var activeProfileId: String? = null

    init {
        viewModelScope.launch {
            preferencesManager.activeProfileIdFlow.collect { profileId ->
                activeProfileId = profileId
                if (profileId != null) {
                    observeAccounts(profileId)
                }
            }
        }
    }

    private fun observeAccounts(profileId: String) {
        viewModelScope.launch {
            getAccountsUseCase(profileId)
                .catch { e -> _uiState.update { it.copy(accountsResource = Resource.Error(e.message ?: "Failed to load accounts")) } }
                .collect { accounts ->
                    _uiState.update {
                        it.copy(
                            accountsResource = if (accounts.isEmpty()) Resource.Empty else Resource.Success(accounts)
                        )
                    }
                }
        }
    }

    fun openAddDialog() {
        _uiState.update { it.copy(isAddDialogOpen = true, userErrorMessage = null) }
    }

    fun closeAddDialog() {
        _uiState.update { it.copy(isAddDialogOpen = false, userErrorMessage = null) }
    }

    fun createAccount(name: String, type: AccountType, initialBalanceStr: String) {
        val profileId = activeProfileId ?: return
        val balance = try {
            if (initialBalanceStr.isBlank()) BigDecimal.ZERO else BigDecimal(initialBalanceStr.trim())
        } catch (e: Exception) {
            _uiState.update { it.copy(userErrorMessage = "Invalid initial balance") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, userErrorMessage = null) }
            val result = createAccountUseCase(
                profileId = profileId,
                name = name,
                type = type,
                initialBalance = balance
            )
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isSubmitting = false, isAddDialogOpen = false) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isSubmitting = false, userErrorMessage = error.message) }
                }
            )
        }
    }

    fun deleteAccount(accountId: String) {
        val profileId = activeProfileId ?: return
        viewModelScope.launch {
            deleteAccountUseCase(profileId, accountId)
        }
    }
}
