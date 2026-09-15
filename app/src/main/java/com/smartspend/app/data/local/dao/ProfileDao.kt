package com.smartspend.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.smartspend.app.data.local.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {

    @Query("SELECT * FROM profiles ORDER BY createdAt ASC")
    fun getAllProfiles(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: String): ProfileEntity?

    @Query("SELECT * FROM profiles WHERE id = :id LIMIT 1")
    fun getProfileFlowById(id: String): Flow<ProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)

    @Update
    suspend fun updateProfile(profile: ProfileEntity)

    @Query("DELETE FROM profiles WHERE id = :id")
    suspend fun deleteProfileById(id: String)

    @Query("DELETE FROM expenses WHERE profileId = :profileId")
    suspend fun deleteExpensesForProfile(profileId: String)

    @Query("DELETE FROM incomes WHERE profileId = :profileId")
    suspend fun deleteIncomesForProfile(profileId: String)

    @Query("DELETE FROM budgets WHERE profileId = :profileId")
    suspend fun deleteBudgetsForProfile(profileId: String)

    @Query("DELETE FROM categories WHERE profileId = :profileId")
    suspend fun deleteCategoriesForProfile(profileId: String)

    @Query("DELETE FROM payment_methods WHERE profileId = :profileId")
    suspend fun deletePaymentMethodsForProfile(profileId: String)

    @Query("DELETE FROM accounts WHERE profileId = :profileId")
    suspend fun deleteAccountsForProfile(profileId: String)

    @Query("DELETE FROM recurring_expenses WHERE profileId = :profileId")
    suspend fun deleteRecurringExpensesForProfile(profileId: String)

    @Query("DELETE FROM savings_goals WHERE profileId = :profileId")
    suspend fun deleteSavingsGoalsForProfile(profileId: String)

    @Query("DELETE FROM savings_goal_contributions WHERE profileId = :profileId")
    suspend fun deleteSavingsGoalContributionsForProfile(profileId: String)

    @Transaction
    suspend fun deleteProfileAndAllData(profileId: String) {
        deleteExpensesForProfile(profileId)
        deleteIncomesForProfile(profileId)
        deleteBudgetsForProfile(profileId)
        deleteCategoriesForProfile(profileId)
        deletePaymentMethodsForProfile(profileId)
        deleteAccountsForProfile(profileId)
        deleteRecurringExpensesForProfile(profileId)
        deleteSavingsGoalsForProfile(profileId)
        deleteSavingsGoalContributionsForProfile(profileId)
        deleteProfileById(profileId)
    }
}
