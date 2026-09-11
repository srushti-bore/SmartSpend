package com.smartspend.app.domain.repository

import com.smartspend.app.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getCategories(profileId: String): Flow<List<Category>>
    suspend fun getCategoryById(profileId: String, id: String): Category?
    suspend fun addCategory(category: Category)
    suspend fun addCategories(categories: List<Category>)
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(profileId: String, id: String)
    suspend fun countExpensesForCategory(profileId: String, categoryId: String): Int
}
