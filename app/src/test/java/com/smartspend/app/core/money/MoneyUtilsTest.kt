package com.smartspend.app.core.money

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class MoneyUtilsTest {

    @Test
    fun `parse valid amount strings returns correct BigDecimal`() {
        val parsed = MoneyUtils.parse("1250.50")
        assertEquals(BigDecimal("1250.50"), parsed)

        val parsedWithCommas = MoneyUtils.parse("1,250.75")
        assertEquals(BigDecimal("1250.75"), parsedWithCommas)
    }

    @Test
    fun `parse invalid amount string returns null`() {
        assertNull(MoneyUtils.parse("abc"))
        assertNull(MoneyUtils.parse(""))
    }

    @Test
    fun `calculatePercentage calculates exact integer percentage`() {
        val part = BigDecimal("800.00")
        val total = BigDecimal("1000.00")
        assertEquals(80, MoneyUtils.calculatePercentage(part, total))

        val partOver = BigDecimal("1200.00")
        assertEquals(120, MoneyUtils.calculatePercentage(partOver, total))
    }

    @Test
    fun `calculatePercentage with zero total returns zero`() {
        val part = BigDecimal("100.00")
        val total = BigDecimal("0.00")
        assertEquals(0, MoneyUtils.calculatePercentage(part, total))
    }
}
