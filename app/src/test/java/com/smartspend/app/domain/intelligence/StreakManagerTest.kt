package com.smartspend.app.domain.intelligence

import com.smartspend.app.domain.model.Expense
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZoneId

class StreakManagerTest {

    private lateinit var streakManager: StreakManager
    private val zoneId = ZoneId.of("Asia/Kolkata")
    private val today = LocalDate.of(2026, 9, 11)

    @Before
    fun setUp() {
        streakManager = StreakManager()
    }

    private fun createExpense(date: LocalDate): Expense {
        val epochMs = date.atStartOfDay(zoneId).toInstant().toEpochMilli() + 3600000L // 1am
        return Expense(
            id = "exp_${date}",
            profileId = "p1",
            title = "Test Expense",
            amount = BigDecimal("100.00"),
            currency = "INR",
            categoryId = "c1",
            paymentMethodId = "pm1",
            date = epochMs
        )
    }

    @Test
    fun `empty expenses returns zero streak`() {
        val status = streakManager.calculateStreak(emptyList(), referenceDate = today)

        assertEquals(0, status.currentStreakDays)
        assertEquals(0, status.longestStreakDays)
        assertFalse(status.isLoggedToday)
        assertFalse(status.badges.first().isUnlocked)
    }

    @Test
    fun `consecutive 3 days logging unlocks 3-Day Starter badge`() {
        val expenses = listOf(
            createExpense(today.minusDays(2)),
            createExpense(today.minusDays(1)),
            createExpense(today)
        )

        val status = streakManager.calculateStreak(expenses, referenceDate = today)

        assertEquals(3, status.currentStreakDays)
        assertEquals(3, status.longestStreakDays)
        assertTrue(status.isLoggedToday)

        val badge3 = status.badges.find { it.id == "badge_3" }
        assertTrue(badge3?.isUnlocked == true)

        val badge7 = status.badges.find { it.id == "badge_7" }
        assertFalse(badge7?.isUnlocked == true)
    }

    @Test
    fun `gap in logging resets current streak but retains longest streak`() {
        val expenses = listOf(
            createExpense(today.minusDays(10)),
            createExpense(today.minusDays(9)),
            createExpense(today.minusDays(8)),
            createExpense(today.minusDays(7)),
            // Gap of 5 days
            createExpense(today.minusDays(1)),
            createExpense(today)
        )

        val status = streakManager.calculateStreak(expenses, referenceDate = today)

        assertEquals(2, status.currentStreakDays)
        assertEquals(4, status.longestStreakDays)
        assertTrue(status.isLoggedToday)
    }
}
