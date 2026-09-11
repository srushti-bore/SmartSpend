package com.smartspend.app.domain.usecase.export

import com.smartspend.app.domain.model.Category
import com.smartspend.app.domain.model.Expense
import com.smartspend.app.domain.model.Income
import com.smartspend.app.domain.model.IncomeSource
import com.smartspend.app.domain.model.PaymentMethod
import com.smartspend.app.domain.model.PaymentType
import com.smartspend.app.domain.usecase.FakeCategoryRepository
import com.smartspend.app.domain.usecase.FakeExpenseRepository
import com.smartspend.app.domain.usecase.FakeIncomeRepository
import com.smartspend.app.domain.usecase.FakePaymentMethodRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class ExportUseCaseTest {

    private lateinit var fakeExpenseRepository: FakeExpenseRepository
    private lateinit var fakeIncomeRepository: FakeIncomeRepository
    private lateinit var fakeCategoryRepository: FakeCategoryRepository
    private lateinit var fakePaymentMethodRepository: FakePaymentMethodRepository
    private lateinit var exportTransactionsUseCase: ExportTransactionsUseCase

    private val profileId = "test_profile_123"

    @Before
    fun setUp() = runTest {
        fakeExpenseRepository = FakeExpenseRepository()
        fakeIncomeRepository = FakeIncomeRepository()
        fakeCategoryRepository = FakeCategoryRepository()
        fakePaymentMethodRepository = FakePaymentMethodRepository()

        fakeCategoryRepository.addCategory(Category("cat_groceries", profileId, "Groceries", "cart", "#FFB3BA"))
        fakePaymentMethodRepository.addPaymentMethod(PaymentMethod("pm_upi", profileId, PaymentType.UPI, "UPI"))

        fakeExpenseRepository.addExpense(
            Expense(
                id = "exp_1",
                profileId = profileId,
                title = "Supermarket Grocery",
                amount = BigDecimal("1540.00"),
                categoryId = "cat_groceries",
                paymentMethodId = "pm_upi",
                date = 1700000000000L
            )
        )

        fakeIncomeRepository.addIncome(
            Income(
                id = "inc_1",
                profileId = profileId,
                source = IncomeSource.FREELANCE,
                title = "Client Project Payment",
                amount = BigDecimal("25000.00"),
                date = 1700000000000L
            )
        )

        exportTransactionsUseCase = ExportTransactionsUseCase(
            fakeExpenseRepository,
            fakeIncomeRepository,
            fakeCategoryRepository,
            fakePaymentMethodRepository
        )
    }

    @Test
    fun `export generates valid csv headers and contents`() = runTest {
        val csv = exportTransactionsUseCase(profileId, 0L, Long.MAX_VALUE)
        assertTrue(csv.contains("Type,Date,Title,Category/Source,Amount,Currency,Payment Method,Notes"))
        assertTrue(csv.contains("EXPENSE"))
        assertTrue(csv.contains("Supermarket Grocery"))
        assertTrue(csv.contains("1540.00"))
        assertTrue(csv.contains("INCOME"))
        assertTrue(csv.contains("Client Project Payment"))
        assertTrue(csv.contains("25000.00"))
    }
}
