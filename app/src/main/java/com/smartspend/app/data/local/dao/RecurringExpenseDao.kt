package com.smartspend.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.smartspend.app.data.local.entity.RecurringExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringExpenseDao {

    @Query("SELECT * FROM recurring_expenses WHERE profileId = :profileId ORDER BY nextDueDate ASC")
    fun getAllRecurringExpenses(profileId: String): Flow<List<RecurringExpenseEntity>>

    @Query("SELECT * FROM recurring_expenses WHERE profileId = :profileId AND isActive = 1 ORDER BY nextDueDate ASC")
    fun getActiveRecurringExpenses(profileId: String): Flow<List<RecurringExpenseEntity>>

    @Query("SELECT * FROM recurring_expenses WHERE profileId = :profileId AND id = :id LIMIT 1")
    suspend fun getRecurringExpenseById(profileId: String, id: String): RecurringExpenseEntity?

    @Query("SELECT * FROM recurring_expenses WHERE isActive = 1 AND nextDueDate <= :cutoffDate")
    suspend fun getDueRecurringExpenses(cutoffDate: Long): List<RecurringExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringExpense(recurringExpense: RecurringExpenseEntity)

    @Update
    suspend fun updateRecurringExpense(recurringExpense: RecurringExpenseEntity)

    @Query("DELETE FROM recurring_expenses WHERE profileId = :profileId AND id = :id")
    suspend fun deleteRecurringExpenseById(profileId: String, id: String)
}
