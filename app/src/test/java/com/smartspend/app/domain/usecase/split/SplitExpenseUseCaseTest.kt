package com.smartspend.app.domain.usecase.split

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class SplitExpenseUseCaseTest {

    private lateinit var useCase: SplitExpenseUseCase

    @Before
    fun setUp() {
        useCase = SplitExpenseUseCase()
    }

    @Test
    fun `calculateEqualSplit divides amount equally among 3 participants with cent rounding`() {
        val total = BigDecimal("1000.00")
        val participants = listOf("You", "Alice", "Bob")

        val result = useCase.calculateEqualSplit(total, participants, "Lunch", "user@upi")

        assertEquals(total, result.totalAmount)
        assertEquals(3, result.participants.size)

        // 1000 / 3 = 333.33 + 0.01 remainder to first person = 333.34
        assertEquals(BigDecimal("333.34"), result.myShare)
        assertEquals(BigDecimal("333.33"), result.participants[1].shareAmount)
        assertEquals(BigDecimal("333.33"), result.participants[2].shareAmount)

        // Sum of all shares must equal exactly 1000.00
        val sum = result.participants.fold(BigDecimal.ZERO) { acc, p -> acc.add(p.shareAmount) }
        assertEquals(total, sum)

        assertTrue(result.shareableText.contains("Lunch"))
        assertTrue(result.shareableText.contains("user@upi"))
    }

    @Test
    fun `calculatePercentageSplit correctly divides custom percentages`() {
        val total = BigDecimal("5000.00")
        val participants = listOf(
            "You" to 50.0,
            "Friend A" to 30.0,
            "Friend B" to 20.0
        )

        val result = useCase.calculatePercentageSplit(total, participants, "Goa Resort", "upi@bank")

        assertEquals(BigDecimal("2500.00"), result.myShare)
        assertEquals(BigDecimal("1500.00"), result.participants[1].shareAmount)
        assertEquals(BigDecimal("1000.00"), result.participants[2].shareAmount)
    }

    @Test
    fun `calculateExactSplit correctly computes exact shares`() {
        val total = BigDecimal("1200.00")
        val participants = listOf(
            "You" to BigDecimal("400.00"),
            "Rohan" to BigDecimal("800.00")
        )

        val result = useCase.calculateExactSplit(total, participants, "Grocery Bill")

        assertEquals(BigDecimal("400.00"), result.myShare)
        assertEquals(BigDecimal("800.00"), result.othersShareTotal)
    }
}
