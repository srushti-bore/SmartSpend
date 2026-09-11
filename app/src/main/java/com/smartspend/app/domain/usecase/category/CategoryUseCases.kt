package com.smartspend.app.domain.usecase.category

import com.smartspend.app.domain.model.Category
import com.smartspend.app.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    operator fun invoke(profileId: String): Flow<List<Category>> {
        return categoryRepository.getCategories(profileId)
    }
}

class AddCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(
        profileId: String,
        name: String,
        iconName: String = "category",
        colorHex: String = "#80B3FF"
    ): Result<Category> {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            return Result.failure(IllegalArgumentException("Category name cannot be empty"))
        }

        val category = Category(
            id = UUID.randomUUID().toString(),
            profileId = profileId,
            name = trimmedName,
            iconName = iconName,
            colorHex = colorHex,
            isCustom = true
        )
        categoryRepository.addCategory(category)
        return Result.success(category)
    }
}

class DeleteCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(profileId: String, categoryId: String): Result<Unit> {
        // Enforce Referential Integrity per SRS §15.3 & FR-P1-006:
        // A Category with existing dependent Expenses cannot be deleted without reassignment.
        val expenseCount = categoryRepository.countExpensesForCategory(profileId, categoryId)
        if (expenseCount > 0) {
            return Result.failure(
                IllegalStateException("Cannot delete category because it contains $expenseCount recorded expenses. Reassign them first.")
            )
        }
        categoryRepository.deleteCategory(profileId, categoryId)
        return Result.success(Unit)
    }
}
