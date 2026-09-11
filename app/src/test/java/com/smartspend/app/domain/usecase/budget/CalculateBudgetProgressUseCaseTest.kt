package com.smartspend.app.domain.usecase.budget

import app.cash.turbine.test
import com.smartspend.app.domain.model.Budget
import com.smartspend.app.domain.model.BudgetStatus
import com.smartspend.app.domain.model.BudgetType
import com.smartspend.app.domain.usecase.FakeExpenseRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class CalculateBudgetProgressUseCaseTest {

    private lateinit var expenseRepository: FakeExpenseRepository
    private lateinit var calculateBudgetProgressUseCase: CalculateBudgetProgressUseCase

    private val profileId = "profile_budget_test"

    @Before
    fun setup() {
        expenseRepository = FakeExpenseRepository()
        calculateBudgetProgressUseCase = CalculateBudgetProgressUseCase(expenseRepository)
    }

    @Test
    fun `budget status is ON_TRACK when spent percentage is below threshold`() = runTest {
        val budget = Budget(
            id = "b1",
            profileId = profileId,
            type = BudgetType.MONTHLY,
            amount = BigDecimal("10000.00"),
            thresholdPct = 80
        )
        expenseRepository.totalSpendingOverride = BigDecimal("5000.00") // 50%

        calculateBudgetProgressUseCase(profileId, budget).test {
            val progress = awaitItem()
            assertEquals(BudgetStatus.ON_TRACK, progress.status)
            assertEquals(50, progress.percentageUsed)
            assertEquals(BigDecimal("5000.00"), progress.remainingAmount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `budget status is NEAR_LIMIT when spent percentage reaches threshold`() = runTest {
        val budget = Budget(
            id = "b2",
            profileId = profileId,
            type = BudgetType.MONTHLY,
            amount = BigDecimal("10000.00"),
            thresholdPct = 80
        )
        expenseRepository.totalSpendingOverride = BigDecimal("8500.00") // 85%

        calculateBudgetProgressUseCase(profileId, budget).test {
            val progress = awaitItem()
            assertEquals(BudgetStatus.NEAR_LIMIT, progress.status)
            assertEquals(85, progress.percentageUsed)
            assertEquals(BigDecimal("1500.00"), progress.remainingAmount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `budget status is OVER_BUDGET when spent exceeds total budget limit`() = runTest {
        val budget = Budget(
            id = "b3",
            profileId = profileId,
            type = BudgetType.MONTHLY,
            amount = BigDecimal("10000.00"),
            thresholdPct = 80
        )
        expenseRepository.totalSpendingOverride = BigDecimal("10500.00") // 105%

        calculateBudgetProgressUseCase(profileId, budget).test {
            val progress = awaitItem()
            assertEquals(BudgetStatus.OVER_BUDGET, progress.status)
            assertEquals(105, progress.percentageUsed)
            assertEquals(BigDecimal("-500.00"), progress.remainingAmount)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
