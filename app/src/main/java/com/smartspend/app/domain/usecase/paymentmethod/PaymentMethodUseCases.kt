package com.smartspend.app.domain.usecase.paymentmethod

import com.smartspend.app.domain.model.PaymentMethod
import com.smartspend.app.domain.model.PaymentType
import com.smartspend.app.domain.repository.PaymentMethodRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class GetPaymentMethodsUseCase @Inject constructor(
    private val paymentMethodRepository: PaymentMethodRepository
) {
    operator fun invoke(profileId: String): Flow<List<PaymentMethod>> {
        return paymentMethodRepository.getPaymentMethods(profileId)
    }
}

class AddPaymentMethodUseCase @Inject constructor(
    private val paymentMethodRepository: PaymentMethodRepository
) {
    suspend operator fun invoke(
        profileId: String,
        type: PaymentType,
        label: String
    ): Result<PaymentMethod> {
        val trimmedLabel = label.trim()
        if (trimmedLabel.isEmpty()) {
            return Result.failure(IllegalArgumentException("Payment method label cannot be empty"))
        }

        val method = PaymentMethod(
            id = UUID.randomUUID().toString(),
            profileId = profileId,
            type = type,
            label = trimmedLabel,
            isCustom = true
        )
        paymentMethodRepository.addPaymentMethod(method)
        return Result.success(method)
    }
}

class DeletePaymentMethodUseCase @Inject constructor(
    private val paymentMethodRepository: PaymentMethodRepository
) {
    suspend operator fun invoke(profileId: String, paymentMethodId: String): Result<Unit> {
        val count = paymentMethodRepository.countExpensesForPaymentMethod(profileId, paymentMethodId)
        if (count > 0) {
            return Result.failure(
                IllegalStateException("Cannot delete payment method because $count expenses are attached to it.")
            )
        }
        paymentMethodRepository.deletePaymentMethod(profileId, paymentMethodId)
        return Result.success(Unit)
    }
}
