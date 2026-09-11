package com.smartspend.app.domain.usecase.account

import com.smartspend.app.domain.model.Account
import com.smartspend.app.domain.repository.AccountRepository
import javax.inject.Inject

class UpdateAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(account: Account): Result<Account> {
        val trimmedName = account.name.trim()
        if (trimmedName.isEmpty()) {
            return Result.failure(IllegalArgumentException("Account name is required"))
        }

        val updated = account.copy(
            name = trimmedName,
            updatedAt = System.currentTimeMillis()
        )
        accountRepository.updateAccount(updated)
        return Result.success(updated)
    }
}
