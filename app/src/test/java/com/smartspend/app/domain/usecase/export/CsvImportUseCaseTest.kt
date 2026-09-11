package com.smartspend.app.domain.usecase.export

import com.smartspend.app.domain.assisted.SmartCategorySuggester
import com.smartspend.app.domain.model.Category
import com.smartspend.app.domain.model.PaymentMethod
import com.smartspend.app.domain.usecase.FakeExpenseRepository
import com.smartspend.app.domain.usecase.expense.DuplicateGuardUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.math.BigDecimal

class CsvImportUseCaseTest {

    private lateinit var csvImportUseCase: CsvImportUseCase
    private lateinit var expenseRepository: FakeExpenseRepository
    private lateinit var categorySuggester: SmartCategorySuggester
    private lateinit var duplicateGuard: DuplicateGuardUseCase

    private val profileId = "test_profile"
    private lateinit var categories: List<Category>
    private lateinit var paymentMethods: List<PaymentMethod>

    @Before
    fun setup() {
        expenseRepository = FakeExpenseRepository()
        categorySuggester = SmartCategorySuggester()
        duplicateGuard = DuplicateGuardUseCase(expenseRepository)
        csvImportUseCase = CsvImportUseCase(expenseRepository, categorySuggester, duplicateGuard)

        categories = Category.starterCategories(profileId)
        paymentMethods = PaymentMethod.starterPaymentMethods(profileId)
    }

    @Test
    fun `parse and commit valid CSV rows successfully`() = runTest {
        val csvData = """
            Date,Title,Amount,Category,Payment Method,Notes
            2026-09-10,Uber Ride,240.00,Transportation,Cash,Office visit
            2026-09-09,Starbucks Coffee,350.00,Food & Dining,UPI,Morning latte
            2026-09-08,Dmart Grocery,1450.50,Groceries,Debit Card,Monthly provisions
        """.trimIndent()

        val inputStream = ByteArrayInputStream(csvData.toByteArray())
        val parseResult = csvImportUseCase.parseCsv(inputStream, profileId, categories, paymentMethods)

        assertEquals(3, parseResult.totalRows)
        assertEquals(3, parseResult.validCandidates.size)
        assertEquals(0, parseResult.errorRowsCount)

        val candidate1 = parseResult.validCandidates[0]
        assertEquals("Uber Ride", candidate1.title)
        assertEquals(BigDecimal("240.00"), candidate1.amount)

        // Commit import
        val importedCount = csvImportUseCase.commitImport(profileId, parseResult.validCandidates)
        assertEquals(3, importedCount)

        val allExpenses = expenseRepository.getAllExpenses(profileId).first()
        assertEquals(3, allExpenses.size)
    }

    @Test
    fun `handles invalid or empty CSV gracefully`() = runTest {
        val emptyStream = ByteArrayInputStream("".toByteArray())
        val result = csvImportUseCase.parseCsv(emptyStream, profileId, categories, paymentMethods)

        assertEquals(0, result.totalRows)
        assertEquals(0, result.validCandidates.size)
    }
}
