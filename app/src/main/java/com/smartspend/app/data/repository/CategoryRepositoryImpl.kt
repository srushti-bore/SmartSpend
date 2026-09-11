package com.smartspend.app.data.repository

import com.smartspend.app.data.local.dao.CategoryDao
import com.smartspend.app.data.mapper.toDomain
import com.smartspend.app.data.mapper.toEntity
import com.smartspend.app.domain.model.Category
import com.smartspend.app.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getCategories(profileId: String): Flow<List<Category>> {
        return categoryDao.getCategoriesForProfile(profileId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getCategoryById(profileId: String, id: String): Category? {
        return categoryDao.getCategoryById(profileId, id)?.toDomain()
    }

    override suspend fun addCategory(category: Category) {
        categoryDao.insertCategory(category.toEntity())
    }

    override suspend fun addCategories(categories: List<Category>) {
        categoryDao.insertCategories(categories.map { it.toEntity() })
    }

    override suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category.toEntity())
    }

    override suspend fun deleteCategory(profileId: String, id: String) {
        categoryDao.deleteCategoryById(profileId, id)
    }

    override suspend fun countExpensesForCategory(profileId: String, categoryId: String): Int {
        return categoryDao.countExpensesForCategory(profileId, categoryId)
    }
}
