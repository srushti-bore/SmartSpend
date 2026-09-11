package com.smartspend.app.domain.usecase.expense

import com.smartspend.app.domain.model.Expense
import com.smartspend.app.domain.model.ExpenseSource
import com.smartspend.app.domain.usecase.FakeExpenseRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

class DuplicateGuardUseCaseTest {

    private lateinit var duplicateGuard: DuplicateGuardUseCase
    private lateinit var expenseRepository: FakeExpenseRepository
    private val profileId = "test_profile"

    @Before
    fun setup() {
        expenseRepository = FakeExpenseRepository()
        duplicateGuard = DuplicateGuardUseCase(expenseRepository)
    }

    @Test
    fun `detects duplicate with exact amount and same title within 3 days`() = runTest {
        val now = System.currentTimeMillis()

        // Existing expense yesterday
        expenseRepository.addExpense(
            Expense(
                id = "exp_1",
                profileId = profileId,
                title = "Uber",
                amount = BigDecimal("240.00"),
                categoryId = "cat_transport",
                paymentMethodId = "pm_cash",
                date = now - TimeUnit.DAYS.toMillis(1)
            )
        )

        val result = duplicateGuard(
            profileId = profileId,
            title = "Uber",
            amount = BigDecimal("240.00"),
            date = now
        )

        assertTrue(result.isDuplicate)
        assertNotNull(result.warningMessage)
        assertTrue(result.warningMessage!!.contains("Uber"))
    }

    @Test
    fun `ignores transaction if amount is different`() = runTest {
        val now = System.currentTimeMillis()

        expenseRepository.addExpense(
            Expense(
                id = "exp_1",
                profileId = profileId,
                title = "Uber",
                amount = BigDecimal("240.00"),
                categoryId = "cat_transport",
                paymentMethodId = "pm_cash",
                date = now
            )
        )

        val result = duplicateGuard(
            profileId = profileId,
            title = "Uber",
            amount = BigDecimal("350.00"),
            date = now
        )

        assertFalse(result.isDuplicate)
    }

    @Test
    fun `ignores transaction if outside 3-day window`() = runTest {
        val now = System.currentTimeMillis()

        // 10 days ago
        expenseRepository.addExpense(
            Expense(
                id = "exp_1",
                profileId = profileId,
                title = "Uber",
                amount = BigDecimal("240.00"),
                categoryId = "cat_transport",
                paymentMethodId = "pm_cash",
                date = now - TimeUnit.DAYS.toMillis(10)
            )
        )

        val result = duplicateGuard(
            profileId = profileId,
            title = "Uber",
            amount = BigDecimal("240.00"),
            date = now
        )

        assertFalse(result.isDuplicate)
    }
}
