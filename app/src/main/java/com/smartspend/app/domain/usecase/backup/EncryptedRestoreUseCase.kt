package com.smartspend.app.domain.usecase.backup

import com.smartspend.app.core.security.KeystoreManager
import com.smartspend.app.domain.model.Account
import com.smartspend.app.domain.model.AccountType
import com.smartspend.app.domain.model.Budget
import com.smartspend.app.domain.model.BudgetType
import com.smartspend.app.domain.model.Category
import com.smartspend.app.domain.model.Expense
import com.smartspend.app.domain.model.ExpenseSource
import com.smartspend.app.domain.model.Income
import com.smartspend.app.domain.model.IncomeSource
import com.smartspend.app.domain.model.PaymentMethod
import com.smartspend.app.domain.model.PaymentType
import com.smartspend.app.domain.model.RecurringExpense
import com.smartspend.app.domain.model.RecurringFrequency
import com.smartspend.app.domain.model.SavingsGoal
import com.smartspend.app.domain.repository.AccountRepository
import com.smartspend.app.domain.repository.BudgetRepository
import com.smartspend.app.domain.repository.CategoryRepository
import com.smartspend.app.domain.repository.ExpenseRepository
import com.smartspend.app.domain.repository.IncomeRepository
import com.smartspend.app.domain.repository.PaymentMethodRepository
import com.smartspend.app.domain.repository.RecurringExpenseRepository
import com.smartspend.app.domain.repository.SavingsGoalRepository
import org.json.JSONObject
import java.io.InputStream
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

data class RestoreSummary(
    val expensesCount: Int,
    val incomesCount: Int,
    val categoriesCount: Int,
    val accountsCount: Int,
    val savingsGoalsCount: Int
)

@Singleton
class EncryptedRestoreUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
    private val budgetRepository: BudgetRepository,
    private val accountRepository: AccountRepository,
    private val recurringRepository: RecurringExpenseRepository,
    private val savingsGoalRepository: SavingsGoalRepository,
    private val keystoreManager: KeystoreManager
) {

    suspend fun restoreBackup(profileId: String, inputStream: InputStream): Result<RestoreSummary> {
        return try {
            val encryptedText = inputStream.bufferedReader().use { it.readText() }
            val decryptedJson = keystoreManager.decrypt(encryptedText)
            val root = JSONObject(decryptedJson)

            val version = root.optInt("version", 1)
            if (version > 1) {
                return Result.failure(IllegalStateException("Unsupported backup version $version"))
            }

            // Restore Categories
            var categoriesRestored = 0
            val catArray = root.optJSONArray("categories")
            if (catArray != null) {
                for (i in 0 until catArray.length()) {
                    val obj = catArray.getJSONObject(i)
                    val cat = Category(
                        id = obj.getString("id"),
                        profileId = profileId,
                        name = obj.getString("name"),
                        iconName = obj.optString("iconName", "category"),
                        colorHex = obj.optString("colorHex", "#80B3FF"),
                        isCustom = obj.optBoolean("isCustom", false)
                    )
                    categoryRepository.addCategory(cat)
                    categoriesRestored++
                }
            }

            // Restore Payment Methods
            val pmArray = root.optJSONArray("paymentMethods")
            if (pmArray != null) {
                for (i in 0 until pmArray.length()) {
                    val obj = pmArray.getJSONObject(i)
                    val pm = PaymentMethod(
                        id = obj.getString("id"),
                        profileId = profileId,
                        type = PaymentType.valueOf(obj.getString("type")),
                        label = obj.getString("label"),
                        isCustom = obj.optBoolean("isCustom", false)
                    )
                    paymentMethodRepository.addPaymentMethod(pm)
                }
            }

            // Restore Incomes
            var incomesRestored = 0
            val incArray = root.optJSONArray("incomes")
            if (incArray != null) {
                for (i in 0 until incArray.length()) {
                    val obj = incArray.getJSONObject(i)
                    val inc = Income(
                        id = obj.getString("id"),
                        profileId = profileId,
                        source = IncomeSource.valueOf(obj.getString("source")),
                        title = obj.optString("title", "Income"),
                        amount = BigDecimal(obj.getString("amount")),
                        currency = obj.optString("currency", "INR"),
                        date = obj.getLong("date"),
                        paymentMethodId = obj.optString("paymentMethodId", "").ifBlank { null },
                        notes = obj.optString("notes", "")
                    )
                    incomeRepository.addIncome(inc)
                    incomesRestored++
                }
            }

            // Restore Expenses
            var expensesRestored = 0
            val expArray = root.optJSONArray("expenses")
            if (expArray != null) {
                for (i in 0 until expArray.length()) {
                    val obj = expArray.getJSONObject(i)
                    val exp = Expense(
                        id = obj.getString("id"),
                        profileId = profileId,
                        title = obj.getString("title"),
                        amount = BigDecimal(obj.getString("amount")),
                        currency = obj.optString("currency", "INR"),
                        categoryId = obj.getString("categoryId"),
                        paymentMethodId = obj.getString("paymentMethodId"),
                        date = obj.getLong("date"),
                        notes = obj.optString("notes", ""),
                        isRecurring = obj.optBoolean("isRecurring", false),
                        source = ExpenseSource.valueOf(obj.optString("source", "MANUAL"))
                    )
                    expenseRepository.addExpense(exp)
                    expensesRestored++
                }
            }

            // Restore Budgets
            val budArray = root.optJSONArray("budgets")
            if (budArray != null) {
                for (i in 0 until budArray.length()) {
                    val obj = budArray.getJSONObject(i)
                    val bud = Budget(
                        id = obj.getString("id"),
                        profileId = profileId,
                        type = BudgetType.valueOf(obj.getString("type")),
                        amount = BigDecimal(obj.getString("amount")),
                        categoryId = obj.optString("categoryId", "").ifBlank { null },
                        thresholdPct = obj.optInt("thresholdPct", 80)
                    )
                    budgetRepository.upsertBudget(bud)
                }
            }

            // Restore Accounts
            var accountsRestored = 0
            val accArray = root.optJSONArray("accounts")
            if (accArray != null) {
                for (i in 0 until accArray.length()) {
                    val obj = accArray.getJSONObject(i)
                    val initBal = BigDecimal(obj.getString("initialBalance"))
                    val currBal = BigDecimal(obj.optString("currentBalance", obj.getString("initialBalance")))
                    val acc = Account(
                        id = obj.getString("id"),
                        profileId = profileId,
                        name = obj.getString("name"),
                        type = AccountType.valueOf(obj.getString("type")),
                        initialBalance = initBal,
                        currentBalance = currBal,
                        isActive = obj.optBoolean("isActive", true)
                    )
                    accountRepository.createAccount(acc)
                    accountsRestored++
                }
            }

            // Restore Recurring Expenses
            val recArray = root.optJSONArray("recurring")
            if (recArray != null) {
                for (i in 0 until recArray.length()) {
                    val obj = recArray.getJSONObject(i)
                    val rec = RecurringExpense(
                        id = obj.getString("id"),
                        profileId = profileId,
                        title = obj.getString("title"),
                        amount = BigDecimal(obj.getString("amount")),
                        categoryId = obj.getString("categoryId"),
                        paymentMethodId = obj.getString("paymentMethodId"),
                        frequency = RecurringFrequency.valueOf(obj.getString("frequency")),
                        startDate = obj.optLong("startDate", System.currentTimeMillis()),
                        nextDueDate = obj.getLong("nextDueDate"),
                        isActive = obj.optBoolean("isActive", true)
                    )
                    recurringRepository.createRecurringExpense(rec)
                }
            }

            // Restore Savings Goals
            var goalsRestored = 0
            val sgArray = root.optJSONArray("savingsGoals")
            if (sgArray != null) {
                for (i in 0 until sgArray.length()) {
                    val obj = sgArray.getJSONObject(i)
                    val goal = SavingsGoal(
                        id = obj.getString("id"),
                        profileId = profileId,
                        name = obj.getString("name"),
                        targetAmount = BigDecimal(obj.getString("targetAmount")),
                        currentAmount = BigDecimal(obj.getString("currentAmount")),
                        targetDate = obj.getLong("targetDate")
                    )
                    savingsGoalRepository.createSavingsGoal(goal)
                    goalsRestored++
                }
            }

            Result.success(
                RestoreSummary(
                    expensesCount = expensesRestored,
                    incomesCount = incomesRestored,
                    categoriesCount = categoriesRestored,
                    accountsCount = accountsRestored,
                    savingsGoalsCount = goalsRestored
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
