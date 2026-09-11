package com.smartspend.app.domain.intelligence

import com.smartspend.app.domain.model.Budget
import com.smartspend.app.domain.model.BudgetType
import com.smartspend.app.domain.usecase.FakeBudgetRepository
import com.smartspend.app.domain.usecase.FakeExpenseRepository
import com.smartspend.app.domain.usecase.FakeIncomeRepository
import com.smartspend.app.domain.usecase.FakeRecurringExpenseRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class PurchaseSimulatorUseCaseTest {

    private lateinit var fakeExpenseRepo: FakeExpenseRepository
    private lateinit var fakeBudgetRepo: FakeBudgetRepository
    private lateinit var fakeRecurringRepo: FakeRecurringExpenseRepository
    private lateinit var fakeIncomeRepo: FakeIncomeRepository
    private lateinit var safeEngine: SafeToSpendEngine
    private lateinit var simulator: PurchaseSimulatorUseCase

    private val profileId = "test_profile"

    @Before
    fun setUp() {
        fakeExpenseRepo = FakeExpenseRepository()
        fakeBudgetRepo = FakeBudgetRepository()
        fakeRecurringRepo = FakeRecurringExpenseRepository()
        fakeIncomeRepo = FakeIncomeRepository()

        safeEngine = SafeToSpendEngine(
            expenseRepository = fakeExpenseRepo,
            budgetRepository = fakeBudgetRepo,
            recurringRepository = fakeRecurringRepo,
            incomeRepository = fakeIncomeRepo
        )
        simulator = PurchaseSimulatorUseCase(safeToSpendEngine = safeEngine)
    }

    @Test
    fun `small purchase within budget returns SAFE_TO_BUY`() = runBlocking {
        fakeBudgetRepo.overallBudgetOverride = Budget(
            id = "b1",
            profileId = profileId,
            type = BudgetType.MONTHLY,
            amount = BigDecimal("50000.00"),
            categoryId = null,
            thresholdPct = 80
        )
        fakeExpenseRepo.totalSpendingOverride = BigDecimal("10000.00")

        val result = simulator(profileId, "Earphones", BigDecimal("1500.00"))

        assertEquals(SimulationDecision.SAFE_TO_BUY, result.decision)
        assertTrue(result.newRemainingBudget > BigDecimal.ZERO)
        assertTrue(result.newSafeDaily > BigDecimal.ZERO)
    }

    @Test
    fun `excessive purchase pushing into deficit returns DELAY_PURCHASE`() = runBlocking {
        fakeBudgetRepo.overallBudgetOverride = Budget(
            id = "b1",
            profileId = profileId,
            type = BudgetType.MONTHLY,
            amount = BigDecimal("20000.00"),
            categoryId = null,
            thresholdPct = 80
        )
        fakeExpenseRepo.totalSpendingOverride = BigDecimal("18000.00")

        val result = simulator(profileId, "Smartphone", BigDecimal("15000.00"))

        assertEquals(SimulationDecision.DELAY_PURCHASE, result.decision)
        assertTrue(result.newRemainingBudget < BigDecimal.ZERO)
        assertEquals(BigDecimal("0.00"), result.newSafeDaily)
    }
}
