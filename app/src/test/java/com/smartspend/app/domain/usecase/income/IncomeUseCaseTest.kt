package com.smartspend.app.domain.usecase.income

import com.smartspend.app.domain.model.IncomeSource
import com.smartspend.app.domain.usecase.FakeExpenseRepository
import com.smartspend.app.domain.usecase.FakeIncomeRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class IncomeUseCaseTest {

    private lateinit var fakeIncomeRepository: FakeIncomeRepository
    private lateinit var fakeExpenseRepository: FakeExpenseRepository
    private lateinit var addIncomeUseCase: AddIncomeUseCase
    private lateinit var getIncomesUseCase: GetIncomesUseCase
    private lateinit var deleteIncomeUseCase: DeleteIncomeUseCase
    private lateinit var getCashFlowSummaryUseCase: GetCashFlowSummaryUseCase

    private val profileId = "test_profile_123"

    @Before
    fun setUp() {
        fakeIncomeRepository = FakeIncomeRepository()
        fakeExpenseRepository = FakeExpenseRepository()
        addIncomeUseCase = AddIncomeUseCase(fakeIncomeRepository)
        getIncomesUseCase = GetIncomesUseCase(fakeIncomeRepository)
        deleteIncomeUseCase = DeleteIncomeUseCase(fakeIncomeRepository)
        getCashFlowSummaryUseCase = GetCashFlowSummaryUseCase(fakeIncomeRepository, fakeExpenseRepository)
    }

    @Test
    fun `addIncome with valid parameters succeeds`() = runTest {
        val result = addIncomeUseCase(
            profileId = profileId,
            title = "Monthly Salary",
            amount = BigDecimal("75000.00"),
            source = IncomeSource.SALARY
        )

        assertTrue(result.isSuccess)
        val income = result.getOrNull()
        assertNotNull(income)
        assertEquals("Monthly Salary", income?.title)
        assertEquals(BigDecimal("75000.00"), income?.amount)
        assertEquals(IncomeSource.SALARY, income?.source)
    }

    @Test
    fun `addIncome with zero or negative amount fails`() = runTest {
        val zeroResult = addIncomeUseCase(
            profileId = profileId,
            title = "Invalid Zero",
            amount = BigDecimal.ZERO,
            source = IncomeSource.OTHER
        )
        assertTrue(zeroResult.isFailure)

        val negativeResult = addIncomeUseCase(
            profileId = profileId,
            title = "Invalid Negative",
            amount = BigDecimal("-500.00"),
            source = IncomeSource.OTHER
        )
        assertTrue(negativeResult.isFailure)
    }

    @Test
    fun `addIncome with empty title fails`() = runTest {
        val result = addIncomeUseCase(
            profileId = profileId,
            title = "   ",
            amount = BigDecimal("1000.00"),
            source = IncomeSource.FREELANCE
        )
        assertTrue(result.isFailure)
    }

    @Test
    fun `cashflow calculation computes net savings accurately`() = runTest {
        fakeIncomeRepository.totalIncomeOverride = BigDecimal("100000.00")
        fakeExpenseRepository.totalSpendingOverride = BigDecimal("40000.00")

        val summary = getCashFlowSummaryUseCase(profileId, 0L, Long.MAX_VALUE).first()
        assertEquals(BigDecimal("100000.00"), summary.totalIncome)
        assertEquals(BigDecimal("40000.00"), summary.totalExpense)
        assertEquals(BigDecimal("60000.00"), summary.netSavings)
        assertEquals(60.0, summary.savingsRatePct, 0.01)
    }
}
