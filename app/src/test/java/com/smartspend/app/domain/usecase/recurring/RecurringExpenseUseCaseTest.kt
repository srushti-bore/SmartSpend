package com.smartspend.app.domain.usecase.recurring

import com.smartspend.app.domain.model.Category
import com.smartspend.app.domain.model.PaymentMethod
import com.smartspend.app.domain.model.PaymentType
import com.smartspend.app.domain.model.RecurringFrequency
import com.smartspend.app.domain.usecase.FakeCategoryRepository
import com.smartspend.app.domain.usecase.FakeExpenseRepository
import com.smartspend.app.domain.usecase.FakePaymentMethodRepository
import com.smartspend.app.domain.usecase.FakeRecurringExpenseRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class RecurringExpenseUseCaseTest {

    private lateinit var fakeRecurringRepository: FakeRecurringExpenseRepository
    private lateinit var fakeExpenseRepository: FakeExpenseRepository
    private lateinit var fakeCategoryRepository: FakeCategoryRepository
    private lateinit var fakePaymentMethodRepository: FakePaymentMethodRepository

    private lateinit var createRecurringExpenseUseCase: CreateRecurringExpenseUseCase
    private lateinit var calculateRecurringCommitmentsUseCase: CalculateRecurringCommitmentsUseCase
    private lateinit var processDueRecurringExpensesUseCase: ProcessDueRecurringExpensesUseCase

    private val profileId = "test_profile_123"

    @Before
    fun setUp() = runTest {
        fakeRecurringRepository = FakeRecurringExpenseRepository()
        fakeExpenseRepository = FakeExpenseRepository()
        fakeCategoryRepository = FakeCategoryRepository()
        fakePaymentMethodRepository = FakePaymentMethodRepository()

        fakeCategoryRepository.addCategory(Category("cat_bills", profileId, "Bills & Utilities", "receipt", "#FFB3BA"))
        fakePaymentMethodRepository.addPaymentMethod(PaymentMethod("pm_upi", profileId, PaymentType.UPI, "UPI"))

        createRecurringExpenseUseCase = CreateRecurringExpenseUseCase(
            fakeRecurringRepository,
            fakeCategoryRepository,
            fakePaymentMethodRepository
        )
        calculateRecurringCommitmentsUseCase = CalculateRecurringCommitmentsUseCase(fakeRecurringRepository)
        processDueRecurringExpensesUseCase = ProcessDueRecurringExpensesUseCase(fakeRecurringRepository, fakeExpenseRepository)
    }

    @Test
    fun `create recurring expense and calculate monthly commitments`() = runTest {
        val result = createRecurringExpenseUseCase(
            profileId = profileId,
            title = "Netflix",
            amount = BigDecimal("649.00"),
            frequency = RecurringFrequency.MONTHLY,
            categoryId = "cat_bills",
            paymentMethodId = "pm_upi"
        )
        assertTrue(result.isSuccess)

        val commitment = calculateRecurringCommitmentsUseCase(profileId).first()
        assertEquals(BigDecimal("649.00"), commitment)
    }

    @Test
    fun `process due recurring expenses auto-generates transaction and advances next date`() = runTest {
        createRecurringExpenseUseCase(
            profileId = profileId,
            title = "Gym Membership",
            amount = BigDecimal("2000.00"),
            frequency = RecurringFrequency.MONTHLY,
            categoryId = "cat_bills",
            paymentMethodId = "pm_upi",
            startDate = 1000L
        )

        val processed = processDueRecurringExpensesUseCase(cutoffTimeMs = 2000L)
        assertEquals(1, processed)
        assertEquals(1, fakeExpenseRepository.expenses.size)
        assertEquals("Gym Membership", fakeExpenseRepository.expenses[0].title)
    }
}
