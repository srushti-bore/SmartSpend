package com.smartspend.app.domain.assisted

import com.smartspend.app.domain.model.ExpenseSource
import com.smartspend.app.domain.model.PaymentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class NaturalLanguageParserTest {

    private lateinit var parser: NaturalLanguageParser
    private lateinit var suggester: SmartCategorySuggester

    @Before
    fun setup() {
        suggester = SmartCategorySuggester()
        parser = NaturalLanguageParser(suggester)
    }

    @Test
    fun `parse standard query with amount title and payment method`() {
        val draft = parser.parse("Uber 240 cash yesterday")

        assertEquals("Uber", draft.title)
        assertEquals(BigDecimal("240.00"), draft.amount)
        assertEquals(PaymentType.CASH, draft.suggestedPaymentType)
        assertEquals("Transportation", draft.suggestedCategoryName)
        assertEquals(ExpenseSource.QUICK_ADD, draft.source)
        assertTrue(draft.confidence > 0.8f)
    }

    @Test
    fun `parse query with rupee symbol and UPI payment`() {
        val draft = parser.parse("Starbucks ₹350.50 via UPI")

        assertEquals("Starbucks", draft.title)
        assertEquals(BigDecimal("350.50"), draft.amount)
        assertEquals(PaymentType.UPI, draft.suggestedPaymentType)
        assertEquals("Food & Dining", draft.suggestedCategoryName)
    }

    @Test
    fun `parse query with notes`() {
        val draft = parser.parse("Dinner 1200 with team")

        assertEquals("Dinner", draft.title)
        assertEquals(BigDecimal("1200.00"), draft.amount)
        assertEquals("team", draft.notes)
    }

    @Test
    fun `parse query with credit card payment`() {
        val draft = parser.parse("Amazon 2499 credit card")

        assertEquals("Amazon", draft.title)
        assertEquals(BigDecimal("2499.00"), draft.amount)
        assertEquals(PaymentType.CREDIT_CARD, draft.suggestedPaymentType)
        assertEquals("Shopping", draft.suggestedCategoryName)
    }

    @Test
    fun `parse handles empty or blank string gracefully`() {
        val draft = parser.parse("")
        assertEquals("", draft.title)
        assertEquals(null, draft.amount)
    }
}
