package com.smartspend.app.domain.repository

import com.smartspend.app.domain.model.PaymentMethod
import kotlinx.coroutines.flow.Flow

interface PaymentMethodRepository {
    fun getPaymentMethods(profileId: String): Flow<List<PaymentMethod>>
    suspend fun getPaymentMethodById(profileId: String, id: String): PaymentMethod?
    suspend fun addPaymentMethod(method: PaymentMethod)
    suspend fun addPaymentMethods(methods: List<PaymentMethod>)
    suspend fun updatePaymentMethod(method: PaymentMethod)
    suspend fun deletePaymentMethod(profileId: String, id: String)
    suspend fun countExpensesForPaymentMethod(profileId: String, paymentMethodId: String): Int
}
