package com.smartspend.app.data.repository

import com.smartspend.app.data.local.dao.PaymentMethodDao
import com.smartspend.app.data.mapper.toDomain
import com.smartspend.app.data.mapper.toEntity
import com.smartspend.app.domain.model.PaymentMethod
import com.smartspend.app.domain.repository.PaymentMethodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentMethodRepositoryImpl @Inject constructor(
    private val paymentMethodDao: PaymentMethodDao
) : PaymentMethodRepository {

    override fun getPaymentMethods(profileId: String): Flow<List<PaymentMethod>> {
        return paymentMethodDao.getPaymentMethodsForProfile(profileId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getPaymentMethodById(profileId: String, id: String): PaymentMethod? {
        return paymentMethodDao.getPaymentMethodById(profileId, id)?.toDomain()
    }

    override suspend fun addPaymentMethod(method: PaymentMethod) {
        paymentMethodDao.insertPaymentMethod(method.toEntity())
    }

    override suspend fun addPaymentMethods(methods: List<PaymentMethod>) {
        paymentMethodDao.insertPaymentMethods(methods.map { it.toEntity() })
    }

    override suspend fun updatePaymentMethod(method: PaymentMethod) {
        paymentMethodDao.updatePaymentMethod(method.toEntity())
    }

    override suspend fun deletePaymentMethod(profileId: String, id: String) {
        paymentMethodDao.deletePaymentMethodById(profileId, id)
    }

    override suspend fun countExpensesForPaymentMethod(profileId: String, paymentMethodId: String): Int {
        return paymentMethodDao.countExpensesForPaymentMethod(profileId, paymentMethodId)
    }
}
