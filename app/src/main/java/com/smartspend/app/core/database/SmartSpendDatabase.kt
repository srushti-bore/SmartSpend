package com.smartspend.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.smartspend.app.data.local.dao.BudgetDao
import com.smartspend.app.data.local.dao.CategoryDao
import com.smartspend.app.data.local.dao.ExpenseDao
import com.smartspend.app.data.local.dao.PaymentMethodDao
import com.smartspend.app.data.local.dao.ProfileDao
import com.smartspend.app.data.local.entity.BudgetEntity
import com.smartspend.app.data.local.entity.CategoryEntity
import com.smartspend.app.data.local.entity.ExpenseEntity
import com.smartspend.app.data.local.entity.PaymentMethodEntity
import com.smartspend.app.data.local.entity.ProfileEntity

@Database(
    entities = [
        ProfileEntity::class,
        CategoryEntity::class,
        PaymentMethodEntity::class,
        ExpenseEntity::class,
        BudgetEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SmartSpendDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun categoryDao(): CategoryDao
    abstract fun paymentMethodDao(): PaymentMethodDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao
}
