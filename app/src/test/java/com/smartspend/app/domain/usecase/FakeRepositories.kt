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

class FakeIncomeRepository : com.smartspend.app.domain.repository.IncomeRepository {
    val incomes = mutableListOf<com.smartspend.app.domain.model.Income>()
    var totalIncomeOverride: BigDecimal = BigDecimal.ZERO

    override fun getAllIncomes(profileId: String): Flow<List<com.smartspend.app.domain.model.Income>> {
        return MutableStateFlow(incomes.filter { it.profileId == profileId })
    }

    override suspend fun getIncomeById(profileId: String, id: String): com.smartspend.app.domain.model.Income? {
        return incomes.find { it.profileId == profileId && it.id == id }
    }

    override fun getIncomesBetweenDates(profileId: String, startDate: Long, endDate: Long): Flow<List<com.smartspend.app.domain.model.Income>> {
        return MutableStateFlow(incomes.filter { it.profileId == profileId && it.date in startDate..endDate })
    }

    override fun getTotalIncome(profileId: String, startDate: Long, endDate: Long): Flow<BigDecimal> {
        return MutableStateFlow(totalIncomeOverride)
    }

    override fun getIncomeSourceAggregates(profileId: String, startDate: Long, endDate: Long): Flow<List<com.smartspend.app.data.local.dao.IncomeSourceSpendAggregate>> {
        return MutableStateFlow(emptyList())
    }

    override suspend fun addIncome(income: com.smartspend.app.domain.model.Income) {
        incomes.add(income)
    }

    override suspend fun updateIncome(income: com.smartspend.app.domain.model.Income) {
        val index = incomes.indexOfFirst { it.id == income.id }
        if (index != -1) incomes[index] = income
    }

    override suspend fun deleteIncome(profileId: String, id: String) {
        incomes.removeAll { it.profileId == profileId && it.id == id }
    }
}

class FakeAccountRepository : com.smartspend.app.domain.repository.AccountRepository {
    val accounts = mutableListOf<com.smartspend.app.domain.model.Account>()

    override fun getActiveAccounts(profileId: String): Flow<List<com.smartspend.app.domain.model.Account>> {
        return MutableStateFlow(accounts.filter { it.profileId == profileId && it.isActive })
    }

    override fun getAllAccounts(profileId: String): Flow<List<com.smartspend.app.domain.model.Account>> {
        return MutableStateFlow(accounts.filter { it.profileId == profileId })
    }

    override suspend fun getAccountById(profileId: String, id: String): com.smartspend.app.domain.model.Account? {
        return accounts.find { it.profileId == profileId && it.id == id }
    }

    override suspend fun createAccount(account: com.smartspend.app.domain.model.Account) {
        accounts.add(account)
    }

    override suspend fun updateAccount(account: com.smartspend.app.domain.model.Account) {
        val index = accounts.indexOfFirst { it.id == account.id }
        if (index != -1) accounts[index] = account
    }

    override suspend fun deleteAccount(profileId: String, id: String) {
        accounts.removeAll { it.profileId == profileId && it.id == id }
    }
}

class FakeRecurringExpenseRepository : com.smartspend.app.domain.repository.RecurringExpenseRepository {
    val recurring = mutableListOf<com.smartspend.app.domain.model.RecurringExpense>()

    override fun getAllRecurringExpenses(profileId: String): Flow<List<com.smartspend.app.domain.model.RecurringExpense>> {
        return MutableStateFlow(recurring.filter { it.profileId == profileId })
    }

    override fun getActiveRecurringExpenses(profileId: String): Flow<List<com.smartspend.app.domain.model.RecurringExpense>> {
        return MutableStateFlow(recurring.filter { it.profileId == profileId && it.isActive })
    }

    override suspend fun getRecurringExpenseById(profileId: String, id: String): com.smartspend.app.domain.model.RecurringExpense? {
        return recurring.find { it.profileId == profileId && it.id == id }
    }

    override suspend fun getDueRecurringExpenses(cutoffDate: Long): List<com.smartspend.app.domain.model.RecurringExpense> {
        return recurring.filter { it.isActive && it.nextDueDate <= cutoffDate }
    }

    override suspend fun createRecurringExpense(recurringExpense: com.smartspend.app.domain.model.RecurringExpense) {
        recurring.add(recurringExpense)
    }

    override suspend fun updateRecurringExpense(recurringExpense: com.smartspend.app.domain.model.RecurringExpense) {
        val index = recurring.indexOfFirst { it.id == recurringExpense.id }
        if (index != -1) recurring[index] = recurringExpense
    }

    override suspend fun deleteRecurringExpense(profileId: String, id: String) {
        recurring.removeAll { it.profileId == profileId && it.id == id }
    }
}

class FakeSavingsGoalRepository : com.smartspend.app.domain.repository.SavingsGoalRepository {
    val goals = mutableListOf<com.smartspend.app.domain.model.SavingsGoal>()
    val contributions = mutableListOf<com.smartspend.app.domain.model.SavingsGoalContribution>()

    override fun getActiveSavingsGoals(profileId: String): Flow<List<com.smartspend.app.domain.model.SavingsGoal>> {
        return MutableStateFlow(goals.filter { it.profileId == profileId && !it.isArchived })
    }

    override fun getAllSavingsGoals(profileId: String): Flow<List<com.smartspend.app.domain.model.SavingsGoal>> {
        return MutableStateFlow(goals.filter { it.profileId == profileId })
    }

    override suspend fun getSavingsGoalById(profileId: String, id: String): com.smartspend.app.domain.model.SavingsGoal? {
        return goals.find { it.profileId == profileId && it.id == id }
    }

    override suspend fun createSavingsGoal(goal: com.smartspend.app.domain.model.SavingsGoal) {
        goals.add(goal)
    }

    override suspend fun updateSavingsGoal(goal: com.smartspend.app.domain.model.SavingsGoal) {
        val index = goals.indexOfFirst { it.id == goal.id }
        if (index != -1) goals[index] = goal
    }

    override suspend fun deleteSavingsGoal(profileId: String, id: String) {
        goals.removeAll { it.profileId == profileId && it.id == id }
    }

    override fun getContributionsForGoal(profileId: String, goalId: String): Flow<List<com.smartspend.app.domain.model.SavingsGoalContribution>> {
        return MutableStateFlow(contributions.filter { it.profileId == profileId && it.goalId == goalId })
    }

    override suspend fun addContribution(contribution: com.smartspend.app.domain.model.SavingsGoalContribution) {
        contributions.add(contribution)
    }

    override suspend fun deleteContribution(profileId: String, id: String) {
        contributions.removeAll { it.profileId == profileId && it.id == id }
    }
}
