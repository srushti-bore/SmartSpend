package com.smartspend.app.domain.intelligence

import com.smartspend.app.domain.model.Budget
import com.smartspend.app.domain.model.BudgetType
import com.smartspend.app.domain.usecase.FakeAccountRepository
import com.smartspend.app.domain.usecase.FakeBudgetRepository
import com.smartspend.app.domain.usecase.FakeCategoryRepository
import com.smartspend.app.domain.usecase.FakeExpenseRepository
import com.smartspend.app.domain.usecase.FakeIncomeRepository
import com.smartspend.app.domain.usecase.FakeRecurringExpenseRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class AskSmartSpendEngineTest {

    private lateinit var fakeExpenseRepo: FakeExpenseRepository
    private lateinit var fakeIncomeRepo: FakeIncomeRepository
    private lateinit var fakeBudgetRepo: FakeBudgetRepository
    private lateinit var fakeAccountRepo: FakeAccountRepository
    private lateinit var fakeRecurringRepo: FakeRecurringExpenseRepository
    private lateinit var fakeCategoryRepo: FakeCategoryRepository

    private lateinit var safeEngine: SafeToSpendEngine
    private lateinit var healthScore: FinancialHealthScoreUseCase
    private lateinit var leakHunter: LeakHunterUseCase
    private lateinit var purchaseSimulator: PurchaseSimulatorUseCase
    private lateinit var forecaster: SpendForecasterUseCase

    private lateinit var askEngine: AskSmartSpendEngine

    private val profileId = "test_profile"

    @Before
    fun setUp() {
        fakeExpenseRepo = FakeExpenseRepository()
        fakeIncomeRepo = FakeIncomeRepository()
        fakeBudgetRepo = FakeBudgetRepository()
        fakeAccountRepo = FakeAccountRepository()
        fakeRecurringRepo = FakeRecurringExpenseRepository()
        fakeCategoryRepo = FakeCategoryRepository()

        safeEngine = SafeToSpendEngine(fakeExpenseRepo, fakeBudgetRepo, fakeRecurringRepo, fakeIncomeRepo)
        healthScore = FinancialHealthScoreUseCase(fakeExpenseRepo, fakeIncomeRepo, fakeBudgetRepo, fakeAccountRepo, fakeRecurringRepo)
        leakHunter = LeakHunterUseCase(fakeExpenseRepo, fakeRecurringRepo)
        purchaseSimulator = PurchaseSimulatorUseCase(safeEngine)
        forecaster = SpendForecasterUseCase(fakeExpenseRepo, fakeIncomeRepo, fakeBudgetRepo, fakeCategoryRepo)

        askEngine = AskSmartSpendEngine(
            safeToSpendEngine = safeEngine,
            healthScoreUseCase = healthScore,
            leakHunterUseCase = leakHunter,
            purchaseSimulator = purchaseSimulator,
            forecasterUseCase = forecaster,
            expenseRepository = fakeExpenseRepo,
            incomeRepository = fakeIncomeRepo,
            recurringRepository = fakeRecurringRepo,
            budgetRepository = fakeBudgetRepo
        )

        fakeBudgetRepo.overallBudgetOverride = Budget(
            id = "b1",
            profileId = profileId,
            type = BudgetType.MONTHLY,
            amount = BigDecimal("50000.00"),
            categoryId = null,
            thresholdPct = 80
        )
        fakeExpenseRepo.totalSpendingOverride = BigDecimal("15000.00")
        fakeIncomeRepo.totalIncomeOverride = BigDecimal("60000.00")
    }

    @Test
    fun `answers safe to spend queries in English`() = runBlocking {
        val reply = askEngine.answerQuestion(profileId, "How much is my safe daily limit?")

        assertNotNull(reply)
        assertFalse(reply.isUser)
        assertTrue(reply.text.contains("Safe-to-Spend Analysis"))
        assertTrue(reply.quickReplies.isNotEmpty())
    }

    @Test
    fun `answers purchase simulation queries in Marathi and English`() = runBlocking {
        val replyEn = askEngine.answerQuestion(profileId, "Can I afford ₹2000 for shoes?")
        assertTrue(replyEn.text.contains("Purchase Simulation Result"))

        val replyMr = askEngine.answerQuestion(profileId, "1500 kharch karu ka")
        assertTrue(replyMr.text.contains("Purchase Simulation Result"))
    }

    @Test
    fun `answers financial health score queries`() = runBlocking {
        val reply = askEngine.answerQuestion(profileId, "What is my financial health score?")
        assertTrue(reply.text.contains("Financial Health Score"))
        assertTrue(reply.text.contains("Pillar Breakdown"))
    }

    @Test
    fun `answers leak hunter and subscription queries`() = runBlocking {
        val reply = askEngine.answerQuestion(profileId, "Show my spending leaks and recurring bills")
        assertTrue(reply.text.contains("Leak Hunter Report"))
    }
}
