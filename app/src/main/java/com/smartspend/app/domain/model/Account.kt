package com.smartspend.app.domain.model

import java.math.BigDecimal

enum class AccountType(val displayName: String) {
    CASH("Cash"),
    BANK("Bank Account"),
    CREDIT_CARD("Credit Card"),
    WALLET("Digital Wallet")
}

data class Account(
    val id: String,
    val profileId: String,
    val name: String,
    val type: AccountType,
    val currency: String = "INR",
    val initialBalance: BigDecimal = BigDecimal.ZERO,
    val currentBalance: BigDecimal = BigDecimal.ZERO,
    val color: String? = null,
    val icon: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
