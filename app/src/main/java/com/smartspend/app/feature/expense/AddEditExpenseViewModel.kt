package com.smartspend.app.feature.expense

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartspend.app.core.datastore.PreferencesManager
import com.smartspend.app.core.money.MoneyUtils
import com.smartspend.app.domain.assisted.SmartCategorySuggester
import com.smartspend.app.domain.model.Category
import com.smartspend.app.domain.model.Expense
import com.smartspend.app.domain.model.ExpenseSource
import com.smartspend.app.domain.model.PaymentMethod
import com.smartspend.app.domain.repository.CategoryRepository
import com.smartspend.app.domain.repository.ExpenseRepository
import com.smartspend.app.domain.repository.PaymentMethodRepository
import com.smartspend.app.domain.usecase.expense.AddExpenseUseCase
import com.smartspend.app.domain.usecase.expense.DeleteExpenseUseCase
import com.smartspend.app.domain.usecase.expense.DuplicateGuardUseCase
import com.smartspend.app.domain.usecase.expense.EditExpenseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

data class AddEditExpenseUiState(
    val isEditMode: Boolean = false,
    val expenseId: String? = null,
    val title: String = "",
    val amountInput: String = "",
    val selectedCategoryId: String? = null,
    val selectedPaymentMethodId: String? = null,
    val date: Long = System.currentTimeMillis(),
    val notes: String = "",
    val isRecurring: Boolean = false,
    val source: ExpenseSource = ExpenseSource.MANUAL,
    val categories: List<Category> = emptyList(),
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val currency: String = "INR",
    val duplicateWarning: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false
)

@HiltViewModel
class AddEditExpenseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val addExpenseUseCase: AddExpenseUseCase,
    private val editExpenseUseCase: EditExpenseUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val duplicateGuardUseCase: DuplicateGuardUseCase,
    private val categorySuggester: SmartCategorySuggester,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val expenseIdArg: String? = savedStateHandle.get<String>("expenseId")
    private val prefillTitle: String? = savedStateHandle.get<String>("prefillTitle")
    private val prefillAmount: String? = savedStateHandle.get<String>("prefillAmount")
    private val prefillNotes: String? = savedStateHandle.get<String>("prefillNotes")
    private val prefillDate: Long? = savedStateHandle.get<Long>("prefillDate")

    private val _uiState = MutableStateFlow(
        AddEditExpenseUiState(
            expenseId = expenseIdArg,
            isEditMode = expenseIdArg != null,
            title = prefillTitle ?: "",
            amountInput = prefillAmount ?: "",
            notes = prefillNotes ?: "",
            date = if (prefillDate != null && prefillDate > 0) prefillDate else System.currentTimeMillis()
        )
    )
    val uiState: StateFlow<AddEditExpenseUiState> = _uiState.asStateFlow()

    private var activeProfileId: String? = null

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val profileId = preferencesManager.activeProfileIdFlow.firstOrNull() ?: return@launch
            activeProfileId = profileId

            val categories = categoryRepository.getCategories(profileId).firstOrNull() ?: emptyList()
            val paymentMethods = paymentMethodRepository.getPaymentMethods(profileId).firstOrNull() ?: emptyList()
            val preferredCurrency = preferencesManager.preferredCurrencyFlow.firstOrNull() ?: "INR"

            var initialCategoryId = _uiState.value.selectedCategoryId
            if (initialCategoryId == null && _uiState.value.title.isNotBlank()) {
                val suggested = categorySuggester.suggestCategory(_uiState.value.title, categories)
                initialCategoryId = suggested?.id ?: categories.firstOrNull()?.id
            } else if (initialCategoryId == null) {
                initialCategoryId = categories.firstOrNull()?.id
            }

            _uiState.value = _uiState.value.copy(
                categories = categories,
                paymentMethods = paymentMethods,
                selectedCategoryId = initialCategoryId,
                selectedPaymentMethodId = _uiState.value.selectedPaymentMethodId ?: paymentMethods.firstOrNull()?.id,
                currency = preferredCurrency
            )

            if (expenseIdArg != null) {
                val expense = expenseRepository.getExpenseById(profileId, expenseIdArg)
                if (expense != null) {
                    _uiState.value = _uiState.value.copy(
                        title = expense.title,
                        amountInput = expense.amount.toPlainString(),
                        selectedCategoryId = expense.categoryId,
                        selectedPaymentMethodId = expense.paymentMethodId,
                        date = expense.date,
                        notes = expense.notes ?: "",
                        isRecurring = expense.isRecurring,
                        source = expense.source,
                        currency = expense.currency
                    )
                }
            }

            checkDuplicate()
        }
    }

    fun onTitleChange(title: String) {
        _uiState.value = _uiState.value.copy(title = title, errorMessage = null)
        // Auto-suggest category if user hasn't explicitly set one or when title matches strongly
        val suggested = categorySuggester.suggestCategory(title, _uiState.value.categories)
        if (suggested != null) {
            _uiState.value = _uiState.value.copy(selectedCategoryId = suggested.id)
        }
        checkDuplicate()
    }

    fun onAmountChange(amount: String) {
        _uiState.value = _uiState.value.copy(amountInput = amount, errorMessage = null)
        checkDuplicate()
    }

    fun onCategorySelect(categoryId: String) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
    }

    fun onPaymentMethodSelect(paymentMethodId: String) {
        _uiState.value = _uiState.value.copy(selectedPaymentMethodId = paymentMethodId)
    }

    fun onDateChange(date: Long) {
        _uiState.value = _uiState.value.copy(date = date)
        checkDuplicate()
    }

    fun onNotesChange(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    fun onRecurringToggle(isRecurring: Boolean) {
        _uiState.value = _uiState.value.copy(isRecurring = isRecurring)
    }

    private fun checkDuplicate() {
        val profileId = activeProfileId ?: return
        val state = _uiState.value
        val parsedAmount = MoneyUtils.parse(state.amountInput)

        if (parsedAmount == null || parsedAmount <= BigDecimal.ZERO || state.title.isBlank()) {
            _uiState.value = state.copy(duplicateWarning = null)
            return
        }

        viewModelScope.launch {
            val result = duplicateGuardUseCase(
                profileId = profileId,
                title = state.title,
                amount = parsedAmount,
                date = state.date,
                excludeExpenseId = state.expenseId
            )
            _uiState.value = _uiState.value.copy(
                duplicateWarning = if (result.isDuplicate) result.warningMessage else null
            )
        }
    }

    fun save() {
        val state = _uiState.value
        val profileId = activeProfileId ?: return

        val parsedAmount = MoneyUtils.parse(state.amountInput)
        if (parsedAmount == null || parsedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            _uiState.value = state.copy(errorMessage = "Please enter a valid positive amount")
            return
        }

        if (state.title.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Please enter an expense title")
            return
        }

        val categoryId = state.selectedCategoryId
        if (categoryId == null) {
            _uiState.value = state.copy(errorMessage = "Please select a category")
            return
        }

        val paymentMethodId = state.selectedPaymentMethodId
        if (paymentMethodId == null) {
            _uiState.value = state.copy(errorMessage = "Please select a payment method")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)

            val result = if (state.isEditMode && state.expenseId != null) {
                editExpenseUseCase(
                    profileId = profileId,
                    expenseId = state.expenseId,
                    title = state.title,
                    amount = parsedAmount,
                    currency = state.currency,
                    categoryId = categoryId,
                    paymentMethodId = paymentMethodId,
                    date = state.date,
                    notes = state.notes,
                    isRecurring = state.isRecurring
                )
            } else {
                addExpenseUseCase(
                    profileId = profileId,
                    title = state.title,
                    amount = parsedAmount,
                    currency = state.currency,
                    categoryId = categoryId,
                    paymentMethodId = paymentMethodId,
                    date = state.date,
                    notes = state.notes,
                    isRecurring = state.isRecurring,
                    source = state.source
                )
            }

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, isSaved = true)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to save expense"
                    )
                }
            )
        }
    }

    fun delete() {
        val state = _uiState.value
        val profileId = activeProfileId ?: return
        val expenseId = state.expenseId ?: return

        viewModelScope.launch {
            deleteExpenseUseCase(profileId, expenseId)
            _uiState.value = _uiState.value.copy(isDeleted = true)
        }
    }
}
