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

class SafeToSpendEngineTest {

    private lateinit var fakeExpenseRepo: FakeExpenseRepository
    private lateinit var fakeBudgetRepo: FakeBudgetRepository
    private lateinit var fakeRecurringRepo: FakeRecurringExpenseRepository
    private lateinit var fakeIncomeRepo: FakeIncomeRepository
    private lateinit var engine: SafeToSpendEngine

    private val profileId = "test_profile"

    @Before
    fun setUp() {
        fakeExpenseRepo = FakeExpenseRepository()
        fakeBudgetRepo = FakeBudgetRepository()
        fakeRecurringRepo = FakeRecurringExpenseRepository()
        fakeIncomeRepo = FakeIncomeRepository()
        engine = SafeToSpendEngine(
            expenseRepository = fakeExpenseRepo,
            budgetRepository = fakeBudgetRepo,
            recurringRepository = fakeRecurringRepo,
            incomeRepository = fakeIncomeRepo
        )
    }

    @Test
    fun `calculate returns HEALTHY tier when spending is low`() = runBlocking {
        fakeBudgetRepo.overallBudgetOverride = Budget(
            id = "b1",
            profileId = profileId,
            type = BudgetType.MONTHLY,
            amount = BigDecimal("50000.00"),
            categoryId = null,
            thresholdPct = 80
        )
        fakeExpenseRepo.totalSpendingOverride = BigDecimal("10000.00") // 20%

        val result = engine.calculate(profileId)

        assertEquals(SafeSpendTier.HEALTHY, result.statusTier)
        assertEquals(20, result.percentageUsed)
        assertTrue(result.safeDailySpend > BigDecimal.ZERO)
        assertTrue(result.remainingBudget.compareTo(BigDecimal("40000.00")) == 0)
    }

    @Test
    fun `calculate returns DANGER tier when spending exceeds budget`() = runBlocking {
        fakeBudgetRepo.overallBudgetOverride = Budget(
            id = "b1",
            profileId = profileId,
            type = BudgetType.MONTHLY,
            amount = BigDecimal("20000.00"),
            categoryId = null,
            thresholdPct = 80
        )
        fakeExpenseRepo.totalSpendingOverride = BigDecimal("25000.00") // 125%

        val result = engine.calculate(profileId)

        assertEquals(SafeSpendTier.DANGER, result.statusTier)
        assertTrue(result.percentageUsed >= 100)
        assertEquals(BigDecimal("0.00"), result.safeDailySpend)
    }
}
