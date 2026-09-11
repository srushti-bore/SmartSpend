package com.smartspend.app.domain.usecase.backup

import com.smartspend.app.core.security.KeystoreManager
import com.smartspend.app.domain.model.AuthType
import com.smartspend.app.domain.model.Category
import com.smartspend.app.domain.model.Expense
import com.smartspend.app.domain.model.PaymentMethod
import com.smartspend.app.domain.model.PaymentType
import com.smartspend.app.domain.model.Profile
import com.smartspend.app.domain.usecase.FakeAccountRepository
import com.smartspend.app.domain.usecase.FakeBudgetRepository
import com.smartspend.app.domain.usecase.FakeCategoryRepository
import com.smartspend.app.domain.usecase.FakeExpenseRepository
import com.smartspend.app.domain.usecase.FakeIncomeRepository
import com.smartspend.app.domain.usecase.FakePaymentMethodRepository
import com.smartspend.app.domain.usecase.FakeProfileRepository
import com.smartspend.app.domain.usecase.FakeRecurringExpenseRepository
import com.smartspend.app.domain.usecase.FakeSavingsGoalRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FileInputStream
import java.math.BigDecimal

class TestKeystoreManager : KeystoreManager() {
    override fun encrypt(plainText: String): String = "ENC:$plainText"
    override fun decrypt(encryptedText: String): String = encryptedText.removePrefix("ENC:")
}

class EncryptedBackupUseCaseTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var fakeProfileRepo: FakeProfileRepository
    private lateinit var fakeCategoryRepo: FakeCategoryRepository
    private lateinit var fakePaymentRepo: FakePaymentMethodRepository
    private lateinit var fakeExpenseRepo: FakeExpenseRepository
    private lateinit var fakeIncomeRepo: FakeIncomeRepository
    private lateinit var fakeBudgetRepo: FakeBudgetRepository
    private lateinit var fakeAccountRepo: FakeAccountRepository
    private lateinit var fakeRecurringRepo: FakeRecurringExpenseRepository
    private lateinit var fakeSavingsRepo: FakeSavingsGoalRepository
    private lateinit var testKeystore: TestKeystoreManager

    private lateinit var backupUseCase: EncryptedBackupUseCase
    private lateinit var restoreUseCase: EncryptedRestoreUseCase

    private val profileId = "profile_123"

    @Before
    fun setUp() {
        fakeProfileRepo = FakeProfileRepository()
        fakeCategoryRepo = FakeCategoryRepository()
        fakePaymentRepo = FakePaymentMethodRepository()
        fakeExpenseRepo = FakeExpenseRepository()
        fakeIncomeRepo = FakeIncomeRepository()
        fakeBudgetRepo = FakeBudgetRepository()
        fakeAccountRepo = FakeAccountRepository()
        fakeRecurringRepo = FakeRecurringExpenseRepository()
        fakeSavingsRepo = FakeSavingsGoalRepository()
        testKeystore = TestKeystoreManager()

        backupUseCase = EncryptedBackupUseCase(
            profileRepository = fakeProfileRepo,
            categoryRepository = fakeCategoryRepo,
            paymentMethodRepository = fakePaymentRepo,
            expenseRepository = fakeExpenseRepo,
            incomeRepository = fakeIncomeRepo,
            budgetRepository = fakeBudgetRepo,
            accountRepository = fakeAccountRepo,
            recurringRepository = fakeRecurringRepo,
            savingsGoalRepository = fakeSavingsRepo,
            keystoreManager = testKeystore
        )

        restoreUseCase = EncryptedRestoreUseCase(
            categoryRepository = fakeCategoryRepo,
            paymentMethodRepository = fakePaymentRepo,
            expenseRepository = fakeExpenseRepo,
            incomeRepository = fakeIncomeRepo,
            budgetRepository = fakeBudgetRepo,
            accountRepository = fakeAccountRepo,
            recurringRepository = fakeRecurringRepo,
            savingsGoalRepository = fakeSavingsRepo,
            keystoreManager = testKeystore
        )
    }

    @Test
    fun `createBackup creates encrypted file and restore accurately restores data`() = runBlocking {
        // 1. Seed source data
        fakeProfileRepo.createProfile(
            Profile(
                id = profileId,
                name = "Srush",
                primaryAuthType = AuthType.PIN,
                credentialSalt = "salt",
                credentialHash = "hash",
                biometricEnabled = false,
                createdAt = 1000L
            )
        )

        fakeCategoryRepo.addCategory(
            Category(id = "cat_1", profileId = profileId, name = "Food & Dining")
        )

        fakePaymentRepo.addPaymentMethod(
            PaymentMethod(id = "pm_1", profileId = profileId, type = PaymentType.UPI, label = "GPay")
        )

        fakeExpenseRepo.addExpense(
            Expense(
                id = "exp_1",
                profileId = profileId,
                title = "Biryani",
                amount = BigDecimal("350.00"),
                categoryId = "cat_1",
                paymentMethodId = "pm_1",
                date = 2000L
            )
        )

        // 2. Perform Backup
        val backupFile = File(tempFolder.root, "smartspend_test_backup.smartspend")
        val backupResult = backupUseCase.createBackup(profileId, backupFile)

        assertTrue(backupResult.isSuccess)
        assertTrue(backupFile.exists())
        assertTrue(backupFile.length() > 0)
        assertTrue(backupFile.readText().startsWith("ENC:"))

        // 3. Clear repositories to simulate new restore
        fakeCategoryRepo.deleteCategory(profileId, "cat_1")
        fakePaymentRepo.deletePaymentMethod(profileId, "pm_1")
        fakeExpenseRepo.deleteExpense(profileId, "exp_1")

        // 4. Perform Restore
        val restoreResult = FileInputStream(backupFile).use { fis ->
            restoreUseCase.restoreBackup(profileId, fis)
        }

        assertTrue(restoreResult.isSuccess)
        val summary = restoreResult.getOrNull()
        assertEquals(1, summary?.expensesCount)
        assertEquals(1, summary?.categoriesCount)

        val restoredExpense = fakeExpenseRepo.getExpenseById(profileId, "exp_1")
        assertEquals("Biryani", restoredExpense?.title)
        assertEquals(BigDecimal("350.00"), restoredExpense?.amount)
    }
}
