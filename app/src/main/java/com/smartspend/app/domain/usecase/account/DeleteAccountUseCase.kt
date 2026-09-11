package com.smartspend.app.domain.usecase.account

import com.smartspend.app.domain.repository.AccountRepository
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(profileId: String, accountId: String): Result<Unit> {
        val account = accountRepository.getAccountById(profileId, accountId)
            ?: return Result.failure(IllegalArgumentException("Account not found"))

        accountRepository.deleteAccount(profileId, account.id)
        return Result.success(Unit)
    }
}
