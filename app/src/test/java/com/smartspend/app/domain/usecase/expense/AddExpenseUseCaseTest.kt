package com.smartspend.app.domain.usecase.expense

import com.smartspend.app.domain.model.Category
import com.smartspend.app.domain.model.PaymentMethod
import com.smartspend.app.domain.model.PaymentType
import com.smartspend.app.domain.usecase.FakeCategoryRepository
import com.smartspend.app.domain.usecase.FakeExpenseRepository
import com.smartspend.app.domain.usecase.FakePaymentMethodRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class AddExpenseUseCaseTest {

    private lateinit var expenseRepository: FakeExpenseRepository
    private lateinit var categoryRepository: FakeCategoryRepository
    private lateinit var paymentMethodRepository: FakePaymentMethodRepository
    private lateinit var addExpenseUseCase: AddExpenseUseCase

    private val profileId = "profile_test_1"
    private val categoryId = "cat_food_1"
    private val paymentMethodId = "pm_upi_1"

    @Before
    fun setup() = runTest {
        expenseRepository = FakeExpenseRepository()
        categoryRepository = FakeCategoryRepository()
        paymentMethodRepository = FakePaymentMethodRepository()

        categoryRepository.addCategory(
            Category(id = categoryId, profileId = profileId, name = "Food")
        )
        paymentMethodRepository.addPaymentMethod(
            PaymentMethod(id = paymentMethodId, profileId = profileId, type = PaymentType.UPI, label = "UPI")
        )

        addExpenseUseCase = AddExpenseUseCase(
            expenseRepository,
            categoryRepository,
            paymentMethodRepository
        )
    }

    @Test
    fun `add valid expense successfully persists record`() = runTest {
        val result = addExpenseUseCase(
            profileId = profileId,
            title = "Lunch",
            amount = BigDecimal("250.00"),
            categoryId = categoryId,
            paymentMethodId = paymentMethodId,
            date = System.currentTimeMillis()
        )

        assertTrue(result.isSuccess)
        assertEquals(1, expenseRepository.expenses.size)
        assertEquals(BigDecimal("250.00"), expenseRepository.expenses[0].amount)
        assertEquals("Lunch", expenseRepository.expenses[0].title)
    }

    @Test
    fun `add expense with zero or negative amount fails validation`() = runTest {
        val zeroResult = addExpenseUseCase(
            profileId = profileId,
            title = "Invalid Zero",
            amount = BigDecimal("0.00"),
            categoryId = categoryId,
            paymentMethodId = paymentMethodId,
            date = System.currentTimeMillis()
        )
        assertTrue(zeroResult.isFailure)

        val negativeResult = addExpenseUseCase(
            profileId = profileId,
            title = "Invalid Negative",
            amount = BigDecimal("-50.00"),
            categoryId = categoryId,
            paymentMethodId = paymentMethodId,
            date = System.currentTimeMillis()
        )
        assertTrue(negativeResult.isFailure)
    }

    @Test
    fun `add expense with empty title fails validation`() = runTest {
        val result = addExpenseUseCase(
            profileId = profileId,
            title = "   ",
            amount = BigDecimal("100.00"),
            categoryId = categoryId,
            paymentMethodId = paymentMethodId,
            date = System.currentTimeMillis()
        )
        assertTrue(result.isFailure)
    }

    @Test
    fun `add expense with future date beyond buffer fails validation`() = runTest {
        val futureDate = System.currentTimeMillis() + 86400000L // 1 day in the future
        val result = addExpenseUseCase(
            profileId = profileId,
            title = "Future Purchase",
            amount = BigDecimal("100.00"),
            categoryId = categoryId,
            paymentMethodId = paymentMethodId,
            date = futureDate
        )
        assertTrue(result.isFailure)
    }
}
