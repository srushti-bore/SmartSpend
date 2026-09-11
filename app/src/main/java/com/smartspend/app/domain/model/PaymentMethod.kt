package com.smartspend.app.domain.model

enum class PaymentType {
    CASH,
    UPI,
    DEBIT_CARD,
    CREDIT_CARD,
    BANK_ACCOUNT,
    DIGITAL_WALLET,
    CUSTOM
}

data class PaymentMethod(
    val id: String,
    val profileId: String,
    val type: PaymentType,
    val label: String,
    val isCustom: Boolean = false
) {
    companion object {
        fun starterPaymentMethods(profileId: String): List<PaymentMethod> = listOf(
            PaymentMethod(id = "pm_upi_$profileId", profileId = profileId, type = PaymentType.UPI, label = "UPI"),
            PaymentMethod(id = "pm_cash_$profileId", profileId = profileId, type = PaymentType.CASH, label = "Cash"),
            PaymentMethod(id = "pm_debit_$profileId", profileId = profileId, type = PaymentType.DEBIT_CARD, label = "Debit Card"),
            PaymentMethod(id = "pm_credit_$profileId", profileId = profileId, type = PaymentType.CREDIT_CARD, label = "Credit Card"),
            PaymentMethod(id = "pm_bank_$profileId", profileId = profileId, type = PaymentType.BANK_ACCOUNT, label = "Bank Account")
        )
    }
}
