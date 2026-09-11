package com.smartspend.app.di

import com.smartspend.app.data.repository.BudgetRepositoryImpl
import com.smartspend.app.data.repository.CategoryRepositoryImpl
import com.smartspend.app.data.repository.ExpenseRepositoryImpl
import com.smartspend.app.data.repository.PaymentMethodRepositoryImpl
import com.smartspend.app.data.repository.ProfileRepositoryImpl
import com.smartspend.app.domain.repository.BudgetRepository
import com.smartspend.app.domain.repository.CategoryRepository
import com.smartspend.app.domain.repository.ExpenseRepository
import com.smartspend.app.domain.repository.PaymentMethodRepository
import com.smartspend.app.domain.repository.ProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindPaymentMethodRepository(impl: PaymentMethodRepositoryImpl): PaymentMethodRepository

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(impl: ExpenseRepositoryImpl): ExpenseRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(impl: BudgetRepositoryImpl): BudgetRepository
}
