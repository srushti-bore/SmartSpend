package com.smartspend.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.smartspend.app.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts WHERE profileId = :profileId AND isActive = 1 ORDER BY createdAt ASC")
    fun getActiveAccounts(profileId: String): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE profileId = :profileId ORDER BY createdAt ASC")
    fun getAllAccounts(profileId: String): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE profileId = :profileId AND id = :id LIMIT 1")
    suspend fun getAccountById(profileId: String, id: String): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAccount(account: AccountEntity)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query("DELETE FROM accounts WHERE profileId = :profileId AND id = :id")
    suspend fun deleteAccountById(profileId: String, id: String)
}
