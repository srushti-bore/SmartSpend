package com.smartspend.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.smartspend.app.data.local.entity.BudgetEntity
import com.smartspend.app.domain.model.BudgetType
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Query("SELECT * FROM budgets WHERE profileId = :profileId")
    fun getBudgetsForProfile(profileId: String): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE profileId = :profileId AND type = :type AND (:categoryId IS NULL AND categoryId IS NULL OR categoryId = :categoryId) LIMIT 1")
    suspend fun getBudget(profileId: String, type: BudgetType, categoryId: String?): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE profileId = :profileId AND type = :type AND categoryId IS NULL LIMIT 1")
    fun getOverallBudgetFlow(profileId: String, type: BudgetType): Flow<BudgetEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBudget(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE profileId = :profileId AND id = :id")
    suspend fun deleteBudgetById(profileId: String, id: String)
}
