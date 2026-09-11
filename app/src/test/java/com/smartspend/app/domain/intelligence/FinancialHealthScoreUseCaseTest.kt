package com.smartspend.app.domain.intelligence

import com.smartspend.app.domain.model.Account
import com.smartspend.app.domain.model.AccountType
import com.smartspend.app.domain.model.Budget
import com.smartspend.app.domain.model.BudgetType
import com.smartspend.app.domain.usecase.FakeAccountRepository
import com.smartspend.app.domain.usecase.FakeBudgetRepository
import com.smartspend.app.domain.usecase.FakeExpenseRepository
import com.smartspend.app.domain.usecase.FakeIncomeRepository
import com.smartspend.app.domain.usecase.FakeRecurringExpenseRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class FinancialHealthScoreUseCaseTest {

    private lateinit var fakeExpenseRepo: FakeExpenseRepository
    private lateinit var fakeIncomeRepo: FakeIncomeRepository
    private lateinit var fakeBudgetRepo: FakeBudgetRepository
    private lateinit var fakeAccountRepo: FakeAccountRepository
    private lateinit var fakeRecurringRepo: FakeRecurringExpenseRepository
    private lateinit var useCase: FinancialHealthScoreUseCase

    private val profileId = "test_profile"

    @Before
    fun setUp() {
        fakeExpenseRepo = FakeExpenseRepository()
        fakeIncomeRepo = FakeIncomeRepository()
        fakeBudgetRepo = FakeBudgetRepository()
        fakeAccountRepo = FakeAccountRepository()
        fakeRecurringRepo = FakeRecurringExpenseRepository()

        useCase = FinancialHealthScoreUseCase(
            expenseRepository = fakeExpenseRepo,
            incomeRepository = fakeIncomeRepo,
            budgetRepository = fakeBudgetRepo,
            accountRepository = fakeAccountRepo,
            recurringRepository = fakeRecurringRepo
        )
    }

    @Test
    fun `high income low spend generates high score and A grade`() = runBlocking {
        fakeIncomeRepo.totalIncomeOverride = BigDecimal("100000.00")
        fakeExpenseRepo.totalSpendingOverride = BigDecimal("30000.00")
        fakeBudgetRepo.overallBudgetOverride = Budget(
            id = "b1",
            profileId = profileId,
            type = BudgetType.MONTHLY,
            amount = BigDecimal("50000.00"),
            categoryId = null,
            thresholdPct = 80
        )
        fakeAccountRepo.createAccount(
            Account(
                id = "acc1",
                profileId = profileId,
                name = "Bank",
                type = AccountType.BANK,
                initialBalance = BigDecimal("150000.00"),
                currentBalance = BigDecimal("150000.00"),
                currency = "INR"
            )
        )

        val report = useCase(profileId)

        assertNotNull(report)
        assertTrue(report.overallScore >= 80)
        assertTrue(report.grade == "A" || report.grade == "A+")
        assertEquals(5, report.pillars.size)
        assertTrue(report.actionRecommendations.isNotEmpty())
    }

    @Test
    fun `deficit spending results in low score and grade D`() = runBlocking {
        fakeIncomeRepo.totalIncomeOverride = BigDecimal("20000.00")
        fakeExpenseRepo.totalSpendingOverride = BigDecimal("40000.00") // Deficit

        val report = useCase(profileId)

        assertNotNull(report)
        assertTrue(report.overallScore < 60)
        assertEquals("D", report.grade)
    }
}
