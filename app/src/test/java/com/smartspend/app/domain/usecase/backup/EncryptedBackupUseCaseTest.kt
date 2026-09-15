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
    override fun encrypt(plainText: String, password: String?): String = "ENC:${password ?: ""}:$plainText"
    override fun decrypt(encryptedText: String, password: String?): String {
        val expectedPrefix = "ENC:${password ?: ""}:"
        if (!encryptedText.startsWith(expectedPrefix)) {
            throw IllegalArgumentException("Incorrect backup password. Please check and try again.")
        }
        return encryptedText.removePrefix(expectedPrefix)
    }
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
        val backupFile = File(tempFolder.root, "expense_backup_2026_09_15.enc")
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

    @Test
    fun `fresh APK restore without targetProfileId recreates profile and all ledger entries`() = runBlocking {
        // 1. Seed source data
        val initialProfile = Profile(
            id = "profile_fresh_123",
            name = "FreshUser",
            primaryAuthType = AuthType.PASSWORD,
            credentialSalt = "somesalt",
            credentialHash = "somehash",
            biometricEnabled = true,
            createdAt = 5000L
        )
        fakeProfileRepo.createProfile(initialProfile)

        fakeCategoryRepo.addCategory(
            Category(id = "cat_groceries", profileId = initialProfile.id, name = "Groceries")
        )

        fakeExpenseRepo.addExpense(
            Expense(
                id = "exp_milk",
                profileId = initialProfile.id,
                title = "Milk & Bread",
                amount = BigDecimal("120.00"),
                categoryId = "cat_groceries",
                paymentMethodId = "pm_cash",
                date = 6000L
            )
        )

        // 2. Perform Backup
        val backupFile = File(tempFolder.root, "expense_backup_fresh_test.enc")
        val backupResult = backupUseCase.createBackup(initialProfile.id, backupFile)
        assertTrue(backupResult.isSuccess)

        // 3. Simulate Fresh App install (wipe all repos)
        fakeProfileRepo.deleteProfile(initialProfile.id)
        fakeCategoryRepo.deleteCategory(initialProfile.id, "cat_groceries")
        fakeExpenseRepo.deleteExpense(initialProfile.id, "exp_milk")

        // 4. Perform Fresh APK Restore (targetProfileId is null)
        val restoreResult = FileInputStream(backupFile).use { fis ->
            restoreUseCase.restoreBackup(fis, targetProfileId = null)
        }

        assertTrue(restoreResult.isSuccess)
        val summary = restoreResult.getOrNull()
        assertEquals(1, summary?.expensesCount)
        assertEquals(1, summary?.categoriesCount)
        assertEquals("profile_fresh_123", summary?.restoredProfileId)

        // Verify profile was recreated
        val restoredProfile = fakeProfileRepo.getProfileById("profile_fresh_123")
        assertEquals("FreshUser", restoredProfile?.name)
        assertEquals(AuthType.PASSWORD, restoredProfile?.primaryAuthType)

        // Verify expense was restored
        val restoredExpense = fakeExpenseRepo.getExpenseById("profile_fresh_123", "exp_milk")
        assertEquals("Milk & Bread", restoredExpense?.title)
        assertEquals(BigDecimal("120.00"), restoredExpense?.amount)
    }

    @Test
    fun `password-protected backup requires correct password to restore and fails with wrong password`() = runBlocking {
        val testProfile = Profile(
            id = "profile_pwd_test",
            name = "SecretUser",
            primaryAuthType = AuthType.PASSWORD,
            credentialSalt = "salt123",
            credentialHash = "hash123",
            biometricEnabled = false,
            createdAt = 7000L
        )
        fakeProfileRepo.createProfile(testProfile)
        fakeCategoryRepo.addCategory(Category(id = "cat_secret", profileId = testProfile.id, name = "Secret Expenses"))

        val backupFile = File(tempFolder.root, "password_protected_backup.enc")
        val correctPassword = "MyStrongPassword@123"
        val wrongPassword = "WrongPassword"

        // 1. Export with password
        val exportResult = backupUseCase.createBackup(testProfile.id, backupFile, password = correctPassword)
        assertTrue(exportResult.isSuccess)

        // 2. Attempt restore with wrong password -> should fail
        val wrongRestoreResult = FileInputStream(backupFile).use { fis ->
            restoreUseCase.restoreBackup(testProfile.id, fis, password = wrongPassword)
        }
        assertTrue(wrongRestoreResult.isFailure)

        // 3. Attempt restore with correct password -> should succeed
        val correctRestoreResult = FileInputStream(backupFile).use { fis ->
            restoreUseCase.restoreBackup(testProfile.id, fis, password = correctPassword)
        }
        assertTrue(correctRestoreResult.isSuccess)
    }
}
