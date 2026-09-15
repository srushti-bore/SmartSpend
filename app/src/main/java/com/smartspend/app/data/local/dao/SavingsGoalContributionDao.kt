package com.smartspend.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.smartspend.app.data.local.entity.SavingsGoalContributionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsGoalContributionDao {

    @Query("SELECT * FROM savings_goal_contributions WHERE profileId = :profileId AND goalId = :goalId ORDER BY date DESC, createdAt DESC")
    fun getContributionsForGoal(profileId: String, goalId: String): Flow<List<SavingsGoalContributionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContribution(contribution: SavingsGoalContributionEntity)

    @Query("DELETE FROM savings_goal_contributions WHERE profileId = :profileId AND id = :id")
    suspend fun deleteContributionById(profileId: String, id: String)
}
