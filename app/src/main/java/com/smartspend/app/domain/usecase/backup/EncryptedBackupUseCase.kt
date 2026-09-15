package com.smartspend.app.domain.usecase.backup

import com.smartspend.app.core.security.KeystoreManager
import com.smartspend.app.domain.model.Account
import com.smartspend.app.domain.model.Budget
import com.smartspend.app.domain.model.Category
import com.smartspend.app.domain.model.Expense
import com.smartspend.app.domain.model.Income
import com.smartspend.app.domain.model.PaymentMethod
import com.smartspend.app.domain.model.Profile
import com.smartspend.app.domain.model.RecurringExpense
import com.smartspend.app.domain.model.SavingsGoal
import com.smartspend.app.domain.repository.AccountRepository
import com.smartspend.app.domain.repository.BudgetRepository
import com.smartspend.app.domain.repository.CategoryRepository
import com.smartspend.app.domain.repository.ExpenseRepository
import com.smartspend.app.domain.repository.IncomeRepository
import com.smartspend.app.domain.repository.PaymentMethodRepository
import com.smartspend.app.domain.repository.ProfileRepository
import com.smartspend.app.domain.repository.RecurringExpenseRepository
import com.smartspend.app.domain.repository.SavingsGoalRepository
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptedBackupUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
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

    suspend fun createBackup(profileId: String, destinationFile: File, password: String? = null): Result<File> {
        return try {
            val profile = profileRepository.getProfileById(profileId)
                ?: return Result.failure(IllegalArgumentException("Profile not found"))

            val categories = categoryRepository.getCategories(profileId).first()
            val paymentMethods = paymentMethodRepository.getPaymentMethods(profileId).first()
            val expenses = expenseRepository.getAllExpenses(profileId).first()
            val incomes = incomeRepository.getAllIncomes(profileId).first()
            val budgets = budgetRepository.getBudgets(profileId).first()
            val accounts = accountRepository.getAllAccounts(profileId).first()
            val recurring = recurringRepository.getAllRecurringExpenses(profileId).first()
            val savingsGoals = savingsGoalRepository.getAllSavingsGoals(profileId).first()

            val rootJson = JSONObject().apply {
                put("version", 2)
                put("profileId", profileId)
                put("profileName", profile.name)
                put("exportedAt", System.currentTimeMillis())

                // Full Profile Entity
                put("profile", JSONObject().apply {
                    put("id", profile.id)
                    put("name", profile.name)
                    put("primaryAuthType", profile.primaryAuthType.name)
                    put("credentialSalt", profile.credentialSalt)
                    put("credentialHash", profile.credentialHash)
                    put("biometricEnabled", profile.biometricEnabled)
                    put("createdAt", profile.createdAt)
                })

                // Categories
                put("categories", JSONArray().apply {
                    categories.forEach {
                        put(JSONObject().apply {
                            put("id", it.id)
                            put("name", it.name)
                            put("iconName", it.iconName)
                            put("colorHex", it.colorHex)
                            put("isCustom", it.isCustom)
                        })
                    }
                })

                // Payment Methods
                put("paymentMethods", JSONArray().apply {
                    paymentMethods.forEach {
                        put(JSONObject().apply {
                            put("id", it.id)
                            put("type", it.type.name)
                            put("label", it.label)
                            put("isCustom", it.isCustom)
                        })
                    }
                })

                // Expenses
                put("expenses", JSONArray().apply {
                    expenses.forEach {
                        put(JSONObject().apply {
                            put("id", it.id)
                            put("title", it.title)
                            put("amount", it.amount.toPlainString())
                            put("currency", it.currency)
                            put("categoryId", it.categoryId)
                            put("paymentMethodId", it.paymentMethodId)
                            put("date", it.date)
                            put("notes", it.notes ?: "")
                            put("isRecurring", it.isRecurring)
                            put("source", it.source.name)
                        })
                    }
                })

                // Incomes
                put("incomes", JSONArray().apply {
                    incomes.forEach {
                        put(JSONObject().apply {
                            put("id", it.id)
                            put("source", it.source.name)
                            put("title", it.title)
                            put("amount", it.amount.toPlainString())
                            put("currency", it.currency)
                            put("date", it.date)
                            put("paymentMethodId", it.paymentMethodId ?: "")
                            put("notes", it.notes ?: "")
                        })
                    }
                })

                // Budgets
                put("budgets", JSONArray().apply {
                    budgets.forEach {
                        put(JSONObject().apply {
                            put("id", it.id)
                            put("type", it.type.name)
                            put("amount", it.amount.toPlainString())
                            put("categoryId", it.categoryId ?: "")
                            put("thresholdPct", it.thresholdPct)
                        })
                    }
                })

                // Accounts
                put("accounts", JSONArray().apply {
                    accounts.forEach {
                        put(JSONObject().apply {
                            put("id", it.id)
                            put("name", it.name)
                            put("type", it.type.name)
                            put("initialBalance", it.initialBalance.toPlainString())
                            put("currentBalance", it.currentBalance.toPlainString())
                            put("isActive", it.isActive)
                        })
                    }
                })

                // Recurring
                put("recurring", JSONArray().apply {
                    recurring.forEach {
                        put(JSONObject().apply {
                            put("id", it.id)
                            put("title", it.title)
                            put("amount", it.amount.toPlainString())
                            put("categoryId", it.categoryId)
                            put("paymentMethodId", it.paymentMethodId)
                            put("frequency", it.frequency.name)
                            put("startDate", it.startDate)
                            put("nextDueDate", it.nextDueDate)
                            put("isActive", it.isActive)
                        })
                    }
                })

                // Savings Goals & Contributions
                put("savingsGoals", JSONArray().apply {
                    savingsGoals.forEach {
                        put(JSONObject().apply {
                            put("id", it.id)
                            put("name", it.name)
                            put("targetAmount", it.targetAmount.toPlainString())
                            put("currentAmount", it.currentAmount.toPlainString())
                            put("targetDate", it.targetDate)
                            put("currency", it.currency)
                            put("color", it.color ?: "")
                            put("icon", it.icon ?: "")
                            put("isArchived", it.isArchived)
                        })
                    }
                })
            }

            val payloadStr = rootJson.toString()
            val encryptedPayload = keystoreManager.encrypt(payloadStr, password)

            destinationFile.parentFile?.mkdirs()
            FileOutputStream(destinationFile).use { fos ->
                fos.write(encryptedPayload.toByteArray(Charsets.UTF_8))
            }

            Result.success(destinationFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
