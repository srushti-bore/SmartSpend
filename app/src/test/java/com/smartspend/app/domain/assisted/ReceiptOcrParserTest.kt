package com.smartspend.app.domain.assisted

import com.smartspend.app.domain.model.ExpenseSource
import com.smartspend.app.domain.model.PaymentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class ReceiptOcrParserTest {

    private lateinit var ocrParser: ReceiptOcrParser
    private lateinit var suggester: SmartCategorySuggester

    @Before
    fun setup() {
        suggester = SmartCategorySuggester()
        ocrParser = ReceiptOcrParser(suggester)
    }

    @Test
    fun `parse restaurant bill with subtotal cgst and grand total`() {
        val ocrSample = """
            TAX INVOICE
            BARBEQUE NATION HOSPITALITY LTD
            Date: 10/09/2026 Time: 20:45
            GSTIN: 27AABCB1234F1Z1
            Item: Buffet Dinner x 2  1600.00
            Subtotal: 1600.00
            CGST 2.5%: 40.00
            SGST 2.5%: 40.00
            Grand Total: ₹ 1,680.00
            Payment Mode: UPI / GPay
            Thank You Visit Again
        """.trimIndent()

        val draft = ocrParser.parse(ocrSample, ExpenseSource.OCR)

        assertTrue(draft.title.contains("BARBEQUE NATION", ignoreCase = true))
        assertEquals(BigDecimal("1680.00"), draft.amount)
        assertEquals(PaymentType.UPI, draft.suggestedPaymentType)
        assertEquals("Food & Dining", draft.suggestedCategoryName)
        assertEquals(ExpenseSource.OCR, draft.source)
    }

    @Test
    fun `parse supermarket invoice with net amount paid`() {
        val ocrSample = """
            D-MART SUPERMARKET
            Avenue Supermarts Ltd.
            Date: 08-09-2026
            Milk 1L: 66.00
            Sugar 5kg: 210.00
            Sunflower Oil: 450.00
            Amount Paid: Rs. 726.00
            Mode: Visa Card
        """.trimIndent()

        val draft = ocrParser.parse(ocrSample, ExpenseSource.OCR)

        assertTrue(draft.title.contains("D-MART", ignoreCase = true))
        assertEquals(BigDecimal("726.00"), draft.amount)
        assertEquals(PaymentType.CREDIT_CARD, draft.suggestedPaymentType)
    }

    @Test
    fun `parse handles empty or unreadable ocr text gracefully`() {
        val draft = ocrParser.parse("")
        assertEquals("", draft.title)
        assertEquals(null, draft.amount)
    }
}
