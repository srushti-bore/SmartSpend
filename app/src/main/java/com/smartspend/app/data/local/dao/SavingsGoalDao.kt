package com.smartspend.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.smartspend.app.data.local.entity.SavingsGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsGoalDao {

    @Query("SELECT * FROM savings_goals WHERE profileId = :profileId AND isArchived = 0 ORDER BY targetDate ASC")
    fun getActiveSavingsGoals(profileId: String): Flow<List<SavingsGoalEntity>>

    @Query("SELECT * FROM savings_goals WHERE profileId = :profileId ORDER BY isArchived ASC, targetDate ASC")
    fun getAllSavingsGoals(profileId: String): Flow<List<SavingsGoalEntity>>

    @Query("SELECT * FROM savings_goals WHERE profileId = :profileId AND id = :id LIMIT 1")
    suspend fun getSavingsGoalById(profileId: String, id: String): SavingsGoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsGoal(goal: SavingsGoalEntity)

    @Update
    suspend fun updateSavingsGoal(goal: SavingsGoalEntity)

    @Query("DELETE FROM savings_goals WHERE profileId = :profileId AND id = :id")
    suspend fun deleteSavingsGoalById(profileId: String, id: String)
}
