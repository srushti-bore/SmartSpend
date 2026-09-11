package com.smartspend.app.domain.usecase.account

import com.smartspend.app.domain.model.Account
import com.smartspend.app.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAccountsUseCase @Inject constructor(
    private val accountRepository: AccountRepository
) {
    operator fun invoke(profileId: String, onlyActive: Boolean = true): Flow<List<Account>> {
        return if (onlyActive) {
            accountRepository.getActiveAccounts(profileId)
        } else {
            accountRepository.getAllAccounts(profileId)
        }
    }
}
