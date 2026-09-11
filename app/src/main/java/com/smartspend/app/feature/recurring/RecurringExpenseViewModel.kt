package com.smartspend.app.feature.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartspend.app.core.common.Resource
import com.smartspend.app.core.datastore.PreferencesManager
import com.smartspend.app.domain.model.RecurringExpense
import com.smartspend.app.domain.model.RecurringFrequency
import com.smartspend.app.domain.repository.CategoryRepository
import com.smartspend.app.domain.repository.PaymentMethodRepository
import com.smartspend.app.domain.usecase.recurring.CalculateRecurringCommitmentsUseCase
import com.smartspend.app.domain.usecase.recurring.CreateRecurringExpenseUseCase
import com.smartspend.app.domain.usecase.recurring.DeleteRecurringExpenseUseCase
import com.smartspend.app.domain.usecase.recurring.GetRecurringExpensesUseCase
import com.smartspend.app.domain.usecase.recurring.ProcessDueRecurringExpensesUseCase
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
class RecurringExpenseViewModel @Inject constructor(
    private val getRecurringExpensesUseCase: GetRecurringExpensesUseCase,
    private val createRecurringExpenseUseCase: CreateRecurringExpenseUseCase,
    private val deleteRecurringExpenseUseCase: DeleteRecurringExpenseUseCase,
    private val calculateRecurringCommitmentsUseCase: CalculateRecurringCommitmentsUseCase,
    private val processDueRecurringExpensesUseCase: ProcessDueRecurringExpensesUseCase,
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecurringExpenseUiState())
    val uiState: StateFlow<RecurringExpenseUiState> = _uiState.asStateFlow()

    private var activeProfileId: String? = null

    init {
        viewModelScope.launch {
            processDueRecurringExpensesUseCase()
            preferencesManager.activeProfileIdFlow.collect { profileId ->
                activeProfileId = profileId
                if (profileId != null) {
                    observeRecurring(profileId)
                    observeCommitments(profileId)
                    loadCategoriesAndPaymentMethods(profileId)
                }
            }
        }
    }

    private fun observeRecurring(profileId: String) {
        viewModelScope.launch {
            getRecurringExpensesUseCase(profileId)
                .catch { e -> _uiState.update { it.copy(recurringResource = Resource.Error(e.message ?: "Failed to load subscriptions")) } }
                .collect { list ->
                    _uiState.update {
                        it.copy(
                            recurringResource = if (list.isEmpty()) Resource.Empty else Resource.Success(list)
                        )
                    }
                }
        }
    }

    private fun observeCommitments(profileId: String) {
        viewModelScope.launch {
            calculateRecurringCommitmentsUseCase(profileId).collect { commitment ->
                _uiState.update { it.copy(monthlyCommitment = commitment) }
            }
        }
    }

    private fun loadCategoriesAndPaymentMethods(profileId: String) {
        viewModelScope.launch {
            categoryRepository.getCategories(profileId).collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
        viewModelScope.launch {
            paymentMethodRepository.getPaymentMethods(profileId).collect { pms ->
                _uiState.update { it.copy(paymentMethods = pms) }
            }
        }
    }

    fun openAddDialog() {
        _uiState.update { it.copy(isAddDialogOpen = true, userErrorMessage = null) }
    }

    fun closeAddDialog() {
        _uiState.update { it.copy(isAddDialogOpen = false, userErrorMessage = null) }
    }

    fun createRecurringExpense(
        title: String,
        amountStr: String,
        frequency: RecurringFrequency,
        categoryId: String,
        paymentMethodId: String,
        notes: String? = null
    ) {
        val profileId = activeProfileId ?: return
        val amount = try {
            BigDecimal(amountStr.trim())
        } catch (e: Exception) {
            _uiState.update { it.copy(userErrorMessage = "Invalid amount") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, userErrorMessage = null) }
            val result = createRecurringExpenseUseCase(
                profileId = profileId,
                title = title,
                amount = amount,
                frequency = frequency,
                categoryId = categoryId,
                paymentMethodId = paymentMethodId,
                notes = notes
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

    fun deleteRecurring(id: String) {
        val profileId = activeProfileId ?: return
        viewModelScope.launch {
            deleteRecurringExpenseUseCase(profileId, id)
        }
    }
}
