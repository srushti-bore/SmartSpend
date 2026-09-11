package com.smartspend.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.smartspend.app.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories WHERE profileId = :profileId ORDER BY name ASC")
    fun getCategoriesForProfile(profileId: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE profileId = :profileId AND id = :id LIMIT 1")
    suspend fun getCategoryById(profileId: String, id: String): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCategory(category: CategoryEntity)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE profileId = :profileId AND id = :id")
    suspend fun deleteCategoryById(profileId: String, id: String)

    @Query("SELECT COUNT(*) FROM expenses WHERE profileId = :profileId AND categoryId = :categoryId")
    suspend fun countExpensesForCategory(profileId: String, categoryId: String): Int
}
