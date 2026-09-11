package com.smartspend.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.smartspend.app.data.local.dao.AccountDao
import com.smartspend.app.data.local.dao.BudgetDao
import com.smartspend.app.data.local.dao.CategoryDao
import com.smartspend.app.data.local.dao.ExpenseDao
import com.smartspend.app.data.local.dao.IncomeDao
import com.smartspend.app.data.local.dao.PaymentMethodDao
import com.smartspend.app.data.local.dao.ProfileDao
import com.smartspend.app.data.local.dao.RecurringExpenseDao
import com.smartspend.app.data.local.dao.SavingsGoalContributionDao
import com.smartspend.app.data.local.dao.SavingsGoalDao
import com.smartspend.app.data.local.entity.AccountEntity
import com.smartspend.app.data.local.entity.BudgetEntity
import com.smartspend.app.data.local.entity.CategoryEntity
import com.smartspend.app.data.local.entity.ExpenseEntity
import com.smartspend.app.data.local.entity.IncomeEntity
import com.smartspend.app.data.local.entity.PaymentMethodEntity
import com.smartspend.app.data.local.entity.ProfileEntity
import com.smartspend.app.data.local.entity.RecurringExpenseEntity
import com.smartspend.app.data.local.entity.SavingsGoalContributionEntity
import com.smartspend.app.data.local.entity.SavingsGoalEntity

@Database(
    entities = [
        ProfileEntity::class,
        CategoryEntity::class,
        PaymentMethodEntity::class,
        ExpenseEntity::class,
        BudgetEntity::class,
        IncomeEntity::class,
        AccountEntity::class,
        RecurringExpenseEntity::class,
        SavingsGoalEntity::class,
        SavingsGoalContributionEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SmartSpendDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun categoryDao(): CategoryDao
    abstract fun paymentMethodDao(): PaymentMethodDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao
    abstract fun incomeDao(): IncomeDao
    abstract fun accountDao(): AccountDao
    abstract fun recurringExpenseDao(): RecurringExpenseDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun savingsGoalContributionDao(): SavingsGoalContributionDao
}
