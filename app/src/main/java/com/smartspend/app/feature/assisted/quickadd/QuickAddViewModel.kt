package com.smartspend.app.feature.assisted.quickadd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartspend.app.core.datastore.PreferencesManager
import com.smartspend.app.domain.assisted.NaturalLanguageParser
import com.smartspend.app.domain.assisted.ParsedExpenseDraft
import com.smartspend.app.domain.assisted.SmartCategorySuggester
import com.smartspend.app.domain.model.Category
import com.smartspend.app.domain.model.ExpenseSource
import com.smartspend.app.domain.model.PaymentMethod
import com.smartspend.app.domain.repository.CategoryRepository
import com.smartspend.app.domain.repository.PaymentMethodRepository
import com.smartspend.app.domain.usecase.expense.AddExpenseUseCase
import com.smartspend.app.domain.usecase.expense.DuplicateGuardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

data class QuickAddUiState(
    val rawInput: String = "",
    val parsedDraft: ParsedExpenseDraft? = null,
    val selectedCategory: Category? = null,
    val selectedPaymentMethod: PaymentMethod? = null,
    val availableCategories: List<Category> = emptyList(),
    val availablePaymentMethods: List<PaymentMethod> = emptyList(),
    val duplicateWarning: String? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class QuickAddViewModel @Inject constructor(
    private val nlpParser: NaturalLanguageParser,
    private val categorySuggester: SmartCategorySuggester,
    private val duplicateGuard: DuplicateGuardUseCase,
    private val addExpenseUseCase: AddExpenseUseCase,
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuickAddUiState())
    val uiState: StateFlow<QuickAddUiState> = _uiState.asStateFlow()

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

            _uiState.value = _uiState.value.copy(
                availableCategories = categories,
                availablePaymentMethods = paymentMethods,
                selectedCategory = categories.firstOrNull(),
                selectedPaymentMethod = paymentMethods.firstOrNull()
            )
        }
    }

    fun onInputChanged(input: String) {
        _uiState.value = _uiState.value.copy(rawInput = input, errorMessage = null)
        if (input.isBlank()) {
            _uiState.value = _uiState.value.copy(
                parsedDraft = null,
                duplicateWarning = null
            )
            return
        }

        val draft = nlpParser.parse(input, ExpenseSource.QUICK_ADD)
        val state = _uiState.value

        // Match category
        val matchedCategory = if (draft.suggestedCategoryName != null) {
            categorySuggester.suggestCategory(draft.title + " " + draft.suggestedCategoryName, state.availableCategories)
                ?: state.availableCategories.find { it.name.equals(draft.suggestedCategoryName, ignoreCase = true) }
        } else {
            categorySuggester.suggestCategory(draft.title, state.availableCategories)
        } ?: state.selectedCategory ?: state.availableCategories.firstOrNull()

        // Match payment method
        val matchedPaymentMethod = if (draft.suggestedPaymentType != null) {
            state.availablePaymentMethods.find { it.type == draft.suggestedPaymentType }
        } else null ?: state.selectedPaymentMethod ?: state.availablePaymentMethods.firstOrNull()

        _uiState.value = state.copy(
            parsedDraft = draft,
            selectedCategory = matchedCategory,
            selectedPaymentMethod = matchedPaymentMethod
        )

        // Check for duplicates
        checkDuplicates(draft.title, draft.amount, draft.date)
    }

    fun onCategorySelected(category: Category) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun onPaymentMethodSelected(paymentMethod: PaymentMethod) {
        _uiState.value = _uiState.value.copy(selectedPaymentMethod = paymentMethod)
    }

    fun onDateChanged(newDate: Long) {
        val currentDraft = _uiState.value.parsedDraft ?: return
        val updatedDraft = currentDraft.copy(date = newDate)
        _uiState.value = _uiState.value.copy(parsedDraft = updatedDraft)
        checkDuplicates(updatedDraft.title, updatedDraft.amount, updatedDraft.date)
    }

    private fun checkDuplicates(title: String, amount: BigDecimal?, date: Long) {
        val profileId = activeProfileId ?: return
        if (amount == null || title.isBlank()) {
            _uiState.value = _uiState.value.copy(duplicateWarning = null)
            return
        }

        viewModelScope.launch {
            val duplicateResult = duplicateGuard(profileId, title, amount, date)
            _uiState.value = _uiState.value.copy(
                duplicateWarning = if (duplicateResult.isDuplicate) duplicateResult.warningMessage else null
            )
        }
    }

    fun saveExpense() {
        val state = _uiState.value
        val profileId = activeProfileId ?: return
        val draft = state.parsedDraft

        if (draft == null || draft.amount == null || draft.amount <= BigDecimal.ZERO) {
            _uiState.value = state.copy(errorMessage = "Please enter an expense with a valid amount (e.g. 'Coffee 150')")
            return
        }

        val category = state.selectedCategory ?: state.availableCategories.firstOrNull()
        if (category == null) {
            _uiState.value = state.copy(errorMessage = "Please select a category")
            return
        }

        val paymentMethod = state.selectedPaymentMethod ?: state.availablePaymentMethods.firstOrNull()
        if (paymentMethod == null) {
            _uiState.value = state.copy(errorMessage = "Please select a payment method")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, errorMessage = null)

            val result = addExpenseUseCase(
                profileId = profileId,
                title = draft.title,
                amount = draft.amount,
                currency = draft.currency,
                categoryId = category.id,
                paymentMethodId = paymentMethod.id,
                date = draft.date,
                notes = draft.notes,
                isRecurring = false,
                source = ExpenseSource.QUICK_ADD
            )

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isSaving = false, isSaved = true)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = error.message ?: "Failed to save expense"
                    )
                }
            )
        }
    }
}
