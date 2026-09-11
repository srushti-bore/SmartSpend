package com.smartspend.app.core.database

import androidx.room.TypeConverter
import com.smartspend.app.domain.model.AuthType
import com.smartspend.app.domain.model.BudgetType
import com.smartspend.app.domain.model.ExpenseSource
import com.smartspend.app.domain.model.PaymentType
import java.math.BigDecimal

class Converters {

    @TypeConverter
    fun fromBigDecimal(value: BigDecimal?): String? {
        return value?.toPlainString()
    }

    @TypeConverter
    fun toBigDecimal(value: String?): BigDecimal? {
        return value?.let { BigDecimal(it) }
    }

    @TypeConverter
    fun fromAuthType(value: AuthType?): String? = value?.name

    @TypeConverter
    fun toAuthType(value: String?): AuthType? = value?.let { AuthType.valueOf(it) }

    @TypeConverter
    fun fromPaymentType(value: PaymentType?): String? = value?.name

    @TypeConverter
    fun toPaymentType(value: String?): PaymentType? = value?.let { PaymentType.valueOf(it) }

    @TypeConverter
    fun fromExpenseSource(value: ExpenseSource?): String? = value?.name

    @TypeConverter
    fun toExpenseSource(value: String?): ExpenseSource? = value?.let { ExpenseSource.valueOf(it) }

    @TypeConverter
    fun fromBudgetType(value: BudgetType?): String? = value?.name

    @TypeConverter
    fun toBudgetType(value: String?): BudgetType? = value?.let { BudgetType.valueOf(it) }

    @TypeConverter
    fun fromIncomeSource(value: com.smartspend.app.domain.model.IncomeSource?): String? = value?.name

    @TypeConverter
    fun toIncomeSource(value: String?): com.smartspend.app.domain.model.IncomeSource? =
        value?.let { com.smartspend.app.domain.model.IncomeSource.valueOf(it) }

    @TypeConverter
    fun fromAccountType(value: com.smartspend.app.domain.model.AccountType?): String? = value?.name

    @TypeConverter
    fun toAccountType(value: String?): com.smartspend.app.domain.model.AccountType? =
        value?.let { com.smartspend.app.domain.model.AccountType.valueOf(it) }

    @TypeConverter
    fun fromRecurringFrequency(value: com.smartspend.app.domain.model.RecurringFrequency?): String? = value?.name

    @TypeConverter
    fun toRecurringFrequency(value: String?): com.smartspend.app.domain.model.RecurringFrequency? =
        value?.let { com.smartspend.app.domain.model.RecurringFrequency.valueOf(it) }

    @TypeConverter
    fun fromContributionType(value: com.smartspend.app.domain.model.ContributionType?): String? = value?.name

    @TypeConverter
    fun toContributionType(value: String?): com.smartspend.app.domain.model.ContributionType? =
        value?.let { com.smartspend.app.domain.model.ContributionType.valueOf(it) }
}
