package com.smartspend.app.domain.usecase.category

import com.smartspend.app.domain.model.Category
import com.smartspend.app.domain.usecase.FakeCategoryRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeleteCategoryUseCaseTest {

    private lateinit var categoryRepository: FakeCategoryRepository
    private lateinit var deleteCategoryUseCase: DeleteCategoryUseCase

    @Before
    fun setup() {
        categoryRepository = FakeCategoryRepository()
        deleteCategoryUseCase = DeleteCategoryUseCase(categoryRepository)
    }

    @Test
    fun `delete category with zero dependent expenses succeeds`() = runTest {
        val cat = Category(id = "cat_1", profileId = "p_1", name = "Test Cat", isCustom = true)
        categoryRepository.addCategory(cat)
        categoryRepository.expenseCountOverride = 0

        val result = deleteCategoryUseCase("p_1", "cat_1")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `delete category with existing dependent expenses fails with referential integrity error`() = runTest {
        val cat = Category(id = "cat_2", profileId = "p_1", name = "Busy Cat", isCustom = true)
        categoryRepository.addCategory(cat)
        categoryRepository.expenseCountOverride = 5

        val result = deleteCategoryUseCase("p_1", "cat_2")
        assertTrue(result.isFailure)
    }
}
