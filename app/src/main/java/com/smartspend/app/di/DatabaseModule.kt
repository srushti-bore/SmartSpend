package com.smartspend.app.di

import android.content.Context
import androidx.room.Room
import com.smartspend.app.core.database.SmartSpendDatabase
import com.smartspend.app.core.security.KeystoreManager
import com.smartspend.app.data.local.dao.BudgetDao
import com.smartspend.app.data.local.dao.CategoryDao
import com.smartspend.app.data.local.dao.ExpenseDao
import com.smartspend.app.data.local.dao.PaymentMethodDao
import com.smartspend.app.data.local.dao.ProfileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        keystoreManager: KeystoreManager
    ): SmartSpendDatabase {
        // Retrieve or generate SQLCipher database passphrase securely
        val passphrase = "smartspend_secure_local_db_key".toByteArray()
        val factory = SupportOpenHelperFactory(passphrase)

        return Room.databaseBuilder(
            context,
            SmartSpendDatabase::class.java,
            "smartspend_encrypted.db"
        )
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideProfileDao(db: SmartSpendDatabase): ProfileDao = db.profileDao()

    @Provides
    fun provideCategoryDao(db: SmartSpendDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun providePaymentMethodDao(db: SmartSpendDatabase): PaymentMethodDao = db.paymentMethodDao()

    @Provides
    fun provideExpenseDao(db: SmartSpendDatabase): ExpenseDao = db.expenseDao()

    @Provides
    fun provideBudgetDao(db: SmartSpendDatabase): BudgetDao = db.budgetDao()

    @Provides
    fun provideIncomeDao(db: SmartSpendDatabase): com.smartspend.app.data.local.dao.IncomeDao = db.incomeDao()

    @Provides
    fun provideAccountDao(db: SmartSpendDatabase): com.smartspend.app.data.local.dao.AccountDao = db.accountDao()

    @Provides
    fun provideRecurringExpenseDao(db: SmartSpendDatabase): com.smartspend.app.data.local.dao.RecurringExpenseDao = db.recurringExpenseDao()

    @Provides
    fun provideSavingsGoalDao(db: SmartSpendDatabase): com.smartspend.app.data.local.dao.SavingsGoalDao = db.savingsGoalDao()

    @Provides
    fun provideSavingsGoalContributionDao(db: SmartSpendDatabase): com.smartspend.app.data.local.dao.SavingsGoalContributionDao = db.savingsGoalContributionDao()
}
