package com.smartspend.app.core.money

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object MoneyUtils {
    val ZERO: BigDecimal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN)

    fun format(amount: BigDecimal, currencyCode: String = "INR"): String {
        return try {
            val currency = Currency.getInstance(currencyCode)
            val symbol = currency.symbol
            val formatter = DecimalFormat("#,##,##0.00")
            "$symbol${formatter.format(amount.setScale(2, RoundingMode.HALF_EVEN))}"
        } catch (e: Exception) {
            val formatter = DecimalFormat("#,##,##0.00")
            "$currencyCode ${formatter.format(amount.setScale(2, RoundingMode.HALF_EVEN))}"
        }
    }

    fun parse(amountString: String): BigDecimal? {
        return try {
            val clean = amountString.trim().replace(",", "")
            if (clean.isEmpty()) null
            else BigDecimal(clean).setScale(2, RoundingMode.HALF_EVEN)
        } catch (e: Exception) {
            null
        }
    }

    fun calculatePercentage(part: BigDecimal, total: BigDecimal): Int {
        if (total.compareTo(BigDecimal.ZERO) <= 0) return 0
        return part.multiply(BigDecimal("100"))
            .divide(total, 0, RoundingMode.HALF_EVEN)
            .toInt()
            .coerceAtLeast(0)
    }
}
