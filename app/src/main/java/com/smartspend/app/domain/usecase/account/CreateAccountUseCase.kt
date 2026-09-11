package com.smartspend.app.domain.usecase.account

import com.smartspend.app.domain.model.Account
import com.smartspend.app.domain.model.AccountType
import com.smartspend.app.domain.repository.AccountRepository
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject

class CreateAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(
        profileId: String,
        name: String,
        type: AccountType,
        currency: String = "INR",
        initialBalance: BigDecimal = BigDecimal.ZERO,
        color: String? = null,
        icon: String? = null
    ): Result<Account> {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            return Result.failure(IllegalArgumentException("Account name is required"))
        }

        val account = Account(
            id = UUID.randomUUID().toString(),
            profileId = profileId,
            name = trimmedName,
            type = type,
            currency = currency,
            initialBalance = initialBalance,
            currentBalance = initialBalance,
            color = color,
            icon = icon,
            isActive = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        accountRepository.createAccount(account)
        return Result.success(account)
    }
}
