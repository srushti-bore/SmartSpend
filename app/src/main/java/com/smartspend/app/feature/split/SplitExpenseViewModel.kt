package com.smartspend.app.feature.split

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartspend.app.core.datastore.PreferencesManager
import com.smartspend.app.domain.model.Category
import com.smartspend.app.domain.model.ExpenseSource
import com.smartspend.app.domain.model.PaymentMethod
import com.smartspend.app.domain.usecase.category.GetCategoriesUseCase
import com.smartspend.app.domain.usecase.expense.AddExpenseUseCase
import com.smartspend.app.domain.usecase.paymentmethod.GetPaymentMethodsUseCase
import com.smartspend.app.domain.usecase.split.SplitExpenseUseCase
import com.smartspend.app.domain.usecase.split.SplitResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

data class SplitExpenseUiState(
    val title: String = "",
    val totalAmount: String = "",
    val participants: List<String> = listOf("You", "Friend 1"),
    val newParticipantName: String = "",
    val upiId: String = "",
    val splitResult: SplitResult? = null,
    val isSavedToLedger: Boolean = false,
    val categories: List<Category> = emptyList(),
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val selectedCategoryId: String? = null,
    val selectedPaymentMethodId: String? = null,
    val errorMessage: String? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class SplitExpenseViewModel @Inject constructor(
    private val splitExpenseUseCase: SplitExpenseUseCase,
    private val addExpenseUseCase: AddExpenseUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getPaymentMethodsUseCase: GetPaymentMethodsUseCase,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplitExpenseUiState())
    val uiState: StateFlow<SplitExpenseUiState> = _uiState.asStateFlow()

    init {
        loadProfileData()
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            val profileId = preferencesManager.activeProfileIdFlow.firstOrNull() ?: return@launch
            val cats = getCategoriesUseCase(profileId).firstOrNull() ?: emptyList()
            val pms = getPaymentMethodsUseCase(profileId).firstOrNull() ?: emptyList()

            _uiState.update {
                it.copy(
                    categories = cats,
                    paymentMethods = pms,
                    selectedCategoryId = cats.firstOrNull()?.id,
                    selectedPaymentMethodId = pms.firstOrNull()?.id
                )
            }
        }
    }

    fun onTitleChanged(title: String) {
        _uiState.update { it.copy(title = title) }
        recalculate()
    }

    fun onTotalAmountChanged(amount: String) {
        _uiState.update { it.copy(totalAmount = amount) }
        recalculate()
    }

    fun onUpiIdChanged(upiId: String) {
        _uiState.update { it.copy(upiId = upiId) }
        recalculate()
    }

    fun onNewParticipantNameChanged(name: String) {
        _uiState.update { it.copy(newParticipantName = name) }
    }

    fun addParticipant() {
        val name = _uiState.value.newParticipantName.trim()
        if (name.isNotBlank()) {
            val updated = _uiState.value.participants + name
            _uiState.update { it.copy(participants = updated, newParticipantName = "") }
            recalculate()
        }
    }

    fun removeParticipant(index: Int) {
        val current = _uiState.value.participants.toMutableList()
        if (index in 0 until current.size && current.size > 1) {
            current.removeAt(index)
            _uiState.update { it.copy(participants = current) }
            recalculate()
        }
    }

    fun onCategorySelected(categoryId: String) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun onPaymentMethodSelected(pmId: String) {
        _uiState.update { it.copy(selectedPaymentMethodId = pmId) }
    }

    private fun recalculate() {
        val state = _uiState.value
        val amount = state.totalAmount.toBigDecimalOrNull() ?: BigDecimal.ZERO
        if (amount > BigDecimal.ZERO && state.participants.isNotEmpty()) {
            val title = state.title.ifBlank { "Split Expense" }
            val res = splitExpenseUseCase.calculateEqualSplit(
                totalAmount = amount,
                participantNames = state.participants,
                expenseTitle = title,
                upiId = state.upiId.ifBlank { null }
            )
            _uiState.update { it.copy(splitResult = res, errorMessage = null) }
        } else {
            _uiState.update { it.copy(splitResult = null) }
        }
    }

    fun saveMyShareToLedger() {
        viewModelScope.launch {
            val state = _uiState.value
            val result = state.splitResult ?: return@launch
            val profileId = preferencesManager.activeProfileIdFlow.firstOrNull() ?: return@launch

            val categoryId = state.selectedCategoryId ?: state.categories.firstOrNull()?.id
            val pmId = state.selectedPaymentMethodId ?: state.paymentMethods.firstOrNull()?.id

            if (categoryId == null || pmId == null) {
                _uiState.update { it.copy(errorMessage = "Please select category and payment method") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }

            val title = if (state.title.isNotBlank()) "${state.title} (My Share)" else "Group Expense (My Share)"
            val addRes = addExpenseUseCase(
                profileId = profileId,
                title = title,
                amount = result.myShare,
                currency = "INR",
                categoryId = categoryId,
                paymentMethodId = pmId,
                date = System.currentTimeMillis(),
                notes = "Split among ${state.participants.size} people: ${state.participants.joinToString(", ")}",
                source = ExpenseSource.MANUAL
            )

            if (addRes.isSuccess) {
                _uiState.update { it.copy(isLoading = false, isSavedToLedger = true, errorMessage = null) }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = addRes.exceptionOrNull()?.message ?: "Failed to log expense") }
            }
        }
    }
}
