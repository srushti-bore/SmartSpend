package com.smartspend.app.core.money

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Currency
import java.util.Locale

object MoneyUtils {
    val ZERO: BigDecimal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN)

    fun getCurrencySymbol(currencyCode: String): String {
        return when (currencyCode.uppercase().trim()) {
            "INR" -> "₹"
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            "JPY" -> "¥"
            "CAD" -> "CA$"
            "AUD" -> "AU$"
            else -> try {
                val curr = Currency.getInstance(currencyCode)
                curr.getSymbol(Locale.US)
            } catch (e: Exception) {
                currencyCode
            }
        }
    }

    fun format(amount: BigDecimal, currencyCode: String = "INR"): String {
        val cleanCode = currencyCode.uppercase().trim().ifBlank { "INR" }
        val symbol = getCurrencySymbol(cleanCode)
        val symbols = DecimalFormatSymbols(Locale.US)

        return try {
            val formattedNumber = when (cleanCode) {
                "INR" -> {
                    // Indian numbering system: lakhs and crores (e.g. 1,23,456.78)
                    val formatter = DecimalFormat("#,##,##0.00", symbols)
                    formatter.format(amount.setScale(2, RoundingMode.HALF_EVEN))
                }
                "JPY" -> {
                    // Japanese Yen has no subunit/decimals
                    val formatter = DecimalFormat("#,##0", symbols)
                    formatter.format(amount.setScale(0, RoundingMode.HALF_EVEN))
                }
                else -> {
                    // Standard Western grouping (e.g. 123,456.78) for USD, EUR, GBP, etc.
                    val formatter = DecimalFormat("#,##0.00", symbols)
                    formatter.format(amount.setScale(2, RoundingMode.HALF_EVEN))
                }
            }
            "$symbol$formattedNumber"
        } catch (e: Exception) {
            "$symbol${amount.setScale(2, RoundingMode.HALF_EVEN)}"
        }
    }

    fun parse(amountString: String): BigDecimal? {
        return try {
            val clean = amountString.trim().replace(",", "").replace("₹", "").replace("$", "").replace("€", "").replace("£", "").replace("¥", "")
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

