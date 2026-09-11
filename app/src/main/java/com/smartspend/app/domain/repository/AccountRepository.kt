package com.smartspend.app.domain.repository

import com.smartspend.app.domain.model.Account
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun getActiveAccounts(profileId: String): Flow<List<Account>>
    fun getAllAccounts(profileId: String): Flow<List<Account>>
    suspend fun getAccountById(profileId: String, id: String): Account?
    suspend fun createAccount(account: Account)
    suspend fun updateAccount(account: Account)
    suspend fun deleteAccount(profileId: String, id: String)
}
