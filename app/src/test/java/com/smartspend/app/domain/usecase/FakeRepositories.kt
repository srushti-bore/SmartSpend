package com.smartspend.app.domain.usecase

import com.smartspend.app.data.local.dao.CategorySpendAggregate
import com.smartspend.app.domain.model.Budget
import com.smartspend.app.domain.model.BudgetType
import com.smartspend.app.domain.model.Category
import com.smartspend.app.domain.model.Expense
import com.smartspend.app.domain.model.PaymentMethod
import com.smartspend.app.domain.repository.BudgetRepository
import com.smartspend.app.domain.repository.CategoryRepository
import com.smartspend.app.domain.repository.ExpenseRepository
import com.smartspend.app.domain.repository.PaymentMethodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal

class FakeCategoryRepository : CategoryRepository {
    private val categories = mutableListOf<Category>()
    var expenseCountOverride: Int = 0

    override fun getCategories(profileId: String): Flow<List<Category>> {
        return MutableStateFlow(categories.filter { it.profileId == profileId })
    }

    override suspend fun getCategoryById(profileId: String, id: String): Category? {
        return categories.find { it.profileId == profileId && it.id == id }
    }

    override suspend fun addCategory(category: Category) {
        categories.add(category)
    }

    override suspend fun addCategories(categories: List<Category>) {
        this.categories.addAll(categories)
    }

    override suspend fun updateCategory(category: Category) {
        val index = categories.indexOfFirst { it.id == category.id }
        if (index != -1) categories[index] = category
    }

    override suspend fun deleteCategory(profileId: String, id: String) {
        categories.removeAll { it.profileId == profileId && it.id == id }
    }

    override suspend fun countExpensesForCategory(profileId: String, categoryId: String): Int {
        return expenseCountOverride
    }
}

class FakePaymentMethodRepository : PaymentMethodRepository {
    private val methods = mutableListOf<PaymentMethod>()

    override fun getPaymentMethods(profileId: String): Flow<List<PaymentMethod>> {
        return MutableStateFlow(methods.filter { it.profileId == profileId })
    }

    override suspend fun getPaymentMethodById(profileId: String, id: String): PaymentMethod? {
        return methods.find { it.profileId == profileId && it.id == id }
    }

    override suspend fun addPaymentMethod(method: PaymentMethod) {
        methods.add(method)
    }

    override suspend fun addPaymentMethods(methods: List<PaymentMethod>) {
        this.methods.addAll(methods)
    }

    override suspend fun updatePaymentMethod(method: PaymentMethod) {
        val index = this.methods.indexOfFirst { it.id == method.id }
        if (index != -1) this.methods[index] = method
    }

    override suspend fun deletePaymentMethod(profileId: String, id: String) {
        methods.removeAll { it.profileId == profileId && it.id == id }
    }

    override suspend fun countExpensesForPaymentMethod(profileId: String, paymentMethodId: String): Int = 0
}

class FakeExpenseRepository : ExpenseRepository {
    val expenses = mutableListOf<Expense>()
    var totalSpendingOverride: BigDecimal = BigDecimal.ZERO

    override fun getAllExpenses(profileId: String): Flow<List<Expense>> {
        return MutableStateFlow(expenses.filter { it.profileId == profileId })
    }

    override suspend fun getExpenseById(profileId: String, id: String): Expense? {
        return expenses.find { it.profileId == profileId && it.id == id }
    }

    override fun getRecentExpenses(profileId: String, limit: Int): Flow<List<Expense>> {
        return MutableStateFlow(expenses.filter { it.profileId == profileId }.take(limit))
    }

    override fun getExpensesBetweenDates(profileId: String, startDate: Long, endDate: Long): Flow<List<Expense>> {
        return MutableStateFlow(expenses.filter { it.profileId == profileId && it.date in startDate..endDate })
    }

    override fun filterExpenses(
        profileId: String,
        query: String?,
        categoryId: String?,
        paymentMethodId: String?,
        startDate: Long?,
        endDate: Long?,
        sortOrder: String
    ): Flow<List<Expense>> {
        return MutableStateFlow(expenses.filter { it.profileId == profileId })
    }

    override fun getCategorySpending(profileId: String, startDate: Long, endDate: Long): Flow<List<CategorySpendAggregate>> {
        return MutableStateFlow(emptyList())
    }

    override fun getTotalSpending(profileId: String, startDate: Long, endDate: Long): Flow<BigDecimal> {
        return MutableStateFlow(totalSpendingOverride)
    }

    override fun getCategoryTotalSpending(profileId: String, categoryId: String, startDate: Long, endDate: Long): Flow<BigDecimal> {
        return MutableStateFlow(totalSpendingOverride)
    }

    override suspend fun addExpense(expense: Expense) {
        expenses.add(expense)
    }

    override suspend fun updateExpense(expense: Expense) {
        val index = expenses.indexOfFirst { it.id == expense.id }
        if (index != -1) expenses[index] = expense
    }

    override suspend fun deleteExpense(profileId: String, id: String) {
        expenses.removeAll { it.profileId == profileId && it.id == id }
    }
}
