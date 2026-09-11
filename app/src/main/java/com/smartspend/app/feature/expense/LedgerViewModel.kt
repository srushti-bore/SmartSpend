package com.smartspend.app.feature.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartspend.app.core.datastore.PreferencesManager
import com.smartspend.app.domain.model.Category
import com.smartspend.app.domain.model.Expense
import com.smartspend.app.domain.model.PaymentMethod
import com.smartspend.app.domain.repository.CategoryRepository
import com.smartspend.app.domain.repository.PaymentMethodRepository
import com.smartspend.app.domain.usecase.expense.GetExpensesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LedgerUiState(
    val expenses: List<Expense> = emptyList(),
    val categories: List<Category> = emptyList(),
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val categoriesMap: Map<String, Category> = emptyMap(),
    val searchQuery: String = "",
    val selectedCategoryId: String? = null,
    val selectedPaymentMethodId: String? = null,
    val sortOrder: String = "DATE_DESC",
    val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LedgerViewModel @Inject constructor(
    private val getExpensesUseCase: GetExpensesUseCase,
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LedgerUiState())
    val uiState: StateFlow<LedgerUiState> = _uiState.asStateFlow()

    private val filterQueryFlow = MutableStateFlow("")
    private val filterCategoryFlow = MutableStateFlow<String?>(null)
    private val filterPaymentMethodFlow = MutableStateFlow<String?>(null)
    private val filterSortFlow = MutableStateFlow("DATE_DESC")

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            preferencesManager.activeProfileIdFlow.collect { profileId ->
                if (profileId != null) {
                    categoryRepository.getCategories(profileId).collect { categories ->
                        _uiState.value = _uiState.value.copy(
                            categories = categories,
                            categoriesMap = categories.associateBy { it.id }
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            preferencesManager.activeProfileIdFlow.collect { profileId ->
                if (profileId != null) {
                    paymentMethodRepository.getPaymentMethods(profileId).collect { methods ->
                        _uiState.value = _uiState.value.copy(paymentMethods = methods)
                    }
                }
            }
        }

        viewModelScope.launch {
            combine(
                preferencesManager.activeProfileIdFlow,
                filterQueryFlow,
                filterCategoryFlow,
                filterPaymentMethodFlow,
                filterSortFlow
            ) { profileId, query, categoryId, paymentMethodId, sort ->
                FilterParams(profileId, query, categoryId, paymentMethodId, sort)
            }.flatMapLatest { params ->
                if (params.profileId != null) {
                    getExpensesUseCase(
                        profileId = params.profileId,
                        query = params.query,
                        categoryId = params.categoryId,
                        paymentMethodId = params.paymentMethodId,
                        sortOrder = params.sort
                    )
                } else {
                    flowOf(emptyList())
                }
            }.collect { expenses ->
                _uiState.value = _uiState.value.copy(
                    expenses = expenses,
                    isLoading = false
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        filterQueryFlow.value = query
    }

    fun onCategoryFilterSelect(categoryId: String?) {
        val newCategory = if (_uiState.value.selectedCategoryId == categoryId) null else categoryId
        _uiState.value = _uiState.value.copy(selectedCategoryId = newCategory)
        filterCategoryFlow.value = newCategory
    }

    fun onSortChange(sort: String) {
        _uiState.value = _uiState.value.copy(sortOrder = sort)
        filterSortFlow.value = sort
    }

    private data class FilterParams(
        val profileId: String?,
        val query: String,
        val categoryId: String?,
        val paymentMethodId: String?,
        val sort: String
    )
}
