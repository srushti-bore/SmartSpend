package com.smartspend.app.data.repository

import com.smartspend.app.data.local.dao.AccountDao
import com.smartspend.app.data.mapper.toDomain
import com.smartspend.app.data.mapper.toEntity
import com.smartspend.app.domain.model.Account
import com.smartspend.app.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao
) : AccountRepository {

    override fun getActiveAccounts(profileId: String): Flow<List<Account>> {
        return accountDao.getActiveAccounts(profileId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllAccounts(profileId: String): Flow<List<Account>> {
        return accountDao.getAllAccounts(profileId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getAccountById(profileId: String, id: String): Account? {
        return accountDao.getAccountById(profileId, id)?.toDomain()
    }

    override suspend fun createAccount(account: Account) {
        accountDao.insertAccount(account.toEntity())
    }

    override suspend fun updateAccount(account: Account) {
        accountDao.updateAccount(account.toEntity())
    }

    override suspend fun deleteAccount(profileId: String, id: String) {
        accountDao.deleteAccountById(profileId, id)
    }
}
