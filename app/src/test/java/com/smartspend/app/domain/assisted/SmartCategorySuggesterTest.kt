package com.smartspend.app.domain.assisted

import com.smartspend.app.domain.model.Category
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class SmartCategorySuggesterTest {

    private lateinit var suggester: SmartCategorySuggester
    private val profileId = "test_profile"
    private lateinit var categories: List<Category>

    @Before
    fun setup() {
        suggester = SmartCategorySuggester()
        categories = Category.starterCategories(profileId) + listOf(
            Category(id = "cat_groceries_$profileId", profileId = profileId, name = "Groceries")
        )
    }

    @Test
    fun `suggestCategory correctly identifies Food and Dining keywords`() {
        val result = suggester.suggestCategory("Zomato dinner order", categories)
        assertNotNull(result)
        assertEquals("Food & Dining", result?.name)
    }

    @Test
    fun `suggestCategory correctly identifies Transportation keywords`() {
        val result = suggester.suggestCategory("Uber ride to airport", categories)
        assertNotNull(result)
        assertEquals("Transportation", result?.name)
    }

    @Test
    fun `suggestCategory correctly identifies Bills and Utilities keywords`() {
        val result = suggester.suggestCategory("MSEDCL Electricity bill payment", categories)
        assertNotNull(result)
        assertEquals("Bills & Utilities", result?.name)
    }

    @Test
    fun `suggestCategory correctly identifies Shopping keywords`() {
        val result = suggester.suggestCategory("Amazon purchase new shoes", categories)
        assertNotNull(result)
        assertEquals("Shopping", result?.name)
    }

    @Test
    fun `suggestCategory correctly identifies Groceries keywords`() {
        val result = suggester.suggestCategory("Dmart monthly groceries and milk", categories)
        assertNotNull(result)
        assertEquals("Groceries", result?.name)
    }

    @Test
    fun `suggestCategoryName returns string name for grocery keyword`() {
        val name = suggester.suggestCategoryName("Blinkit vegetables and milk")
        assertEquals("Groceries", name)
    }

    @Test
    fun `suggestCategory returns null for unknown text without matches`() {
        val result = suggester.suggestCategory("Xyz123 random meaningless string", categories)
        assertNull(result)
    }
}
