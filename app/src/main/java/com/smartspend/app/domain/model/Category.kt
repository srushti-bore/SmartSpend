package com.smartspend.app.domain.model

data class Category(
    val id: String,
    val profileId: String,
    val name: String,
    val iconName: String = "category",
    val colorHex: String = "#80B3FF",
    val isCustom: Boolean = false,
    val parentDefaultId: String? = null
) {
    companion object {
        fun starterCategories(profileId: String): List<Category> = listOf(
            Category(id = "cat_food_$profileId", profileId = profileId, name = "Food & Dining", iconName = "restaurant", colorHex = "#FFB3BA"),
            Category(id = "cat_transport_$profileId", profileId = profileId, name = "Transportation", iconName = "directions_car", colorHex = "#BAE1FF"),
            Category(id = "cat_housing_$profileId", profileId = profileId, name = "Housing & Rent", iconName = "home", colorHex = "#BAFFC9"),
            Category(id = "cat_bills_$profileId", profileId = profileId, name = "Bills & Utilities", iconName = "receipt", colorHex = "#FFFFBA"),
            Category(id = "cat_shopping_$profileId", profileId = profileId, name = "Shopping", iconName = "shopping_bag", colorHex = "#E8BAFF"),
            Category(id = "cat_entertainment_$profileId", profileId = profileId, name = "Entertainment", iconName = "movie", colorHex = "#FFDFBA"),
            Category(id = "cat_healthcare_$profileId", profileId = profileId, name = "Healthcare", iconName = "local_hospital", colorHex = "#FFC6FF"),
            Category(id = "cat_education_$profileId", profileId = profileId, name = "Education", iconName = "school", colorHex = "#BDB2FF"),
            Category(id = "cat_other_$profileId", profileId = profileId, name = "Other Expenses", iconName = "more_horiz", colorHex = "#CAFFBF")
        )
    }
}
