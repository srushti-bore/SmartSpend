package com.smartspend.app.domain.assisted

import com.smartspend.app.domain.model.Category
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmartCategorySuggester @Inject constructor() {

    private val keywordCategoryMap: Map<String, List<String>> = mapOf(
        "Food & Dining" to listOf(
            "swiggy", "zomato", "starbucks", "cafe", "mcdonalds", "burger", "pizza", "restaurant",
            "biryani", "dining", "chai", "tea", "coffee", "breakfast", "lunch", "dinner", "kfc",
            "domino", "subway", "bakery", "sweets", "food", "barbeque", "dhaba", "canteen", "bistro", "tiffin"
        ),
        "Transportation" to listOf(
            "uber", "ola", "rapido", "cab", "taxi", "auto", "metro", "bus", "petrol", "diesel",
            "fuel", "cng", "irctc", "train", "railway", "flight", "indigo", "air india", "toll",
            "fastag", "parking", "transport", "fare", "commute"
        ),
        "Housing & Rent" to listOf(
            "rent", "landlord", "maintenance", "society", "lease", "house", "flat", "apartment", "mortgage"
        ),
        "Bills & Utilities" to listOf(
            "electricity", "msedcl", "bescom", "water", "gas", "cylinder", "indane", "hp gas",
            "bharat gas", "wifi", "broadband", "internet", "recharge", "jio", "airtel", "vi",
            "bsnl", "dth", "tata play", "bill", "utility", "piped gas"
        ),
        "Shopping" to listOf(
            "amazon", "flipkart", "myntra", "ajio", "zara", "h&m", "meesho", "nykaa", "clothes",
            "shoes", "electronics", "shopping", "apple", "croma", "reliance digital", "purchase", "mall"
        ),
        "Groceries" to listOf(
            "dmart", "blinkit", "zepto", "instamart", "bigbasket", "supermarket", "grocery",
            "groceries", "vegetables", "fruits", "sabzi", "milk", "dairy", "ration", "provisions"
        ),
        "Entertainment" to listOf(
            "netflix", "spotify", "hotstar", "prime video", "movie", "cinema", "pvr", "inox",
            "bookmyshow", "concert", "game", "gaming", "steam", "youtube", "playstation", "entertainment"
        ),
        "Healthcare" to listOf(
            "pharmacy", "medical", "medicine", "doctor", "hospital", "clinic", "1mg", "apollo",
            "pharmeasy", "dental", "dentist", "lab", "blood test", "health", "consultation", "tablets"
        ),
        "Education" to listOf(
            "fee", "fees", "tuition", "coaching", "school", "college", "university", "udemy",
            "coursera", "books", "stationery", "course", "exam", "classes"
        )
    )

    fun suggestCategory(text: String, availableCategories: List<Category>): Category? {
        if (text.isBlank() || availableCategories.isEmpty()) return null

        val normalized = text.lowercase()

        // 1. Check exact match with existing category names
        val directMatch = availableCategories.find { normalized.contains(it.name.lowercase()) }
        if (directMatch != null) return directMatch

        // 2. Check keyword mappings
        for ((targetCategoryName, keywords) in keywordCategoryMap) {
            if (keywords.any { keyword ->
                    val pattern = "\\b${Regex.escape(keyword)}\\b".toRegex()
                    pattern.containsMatchIn(normalized)
                }) {
                // Find matching category in available categories
                val matched = availableCategories.find {
                    it.name.equals(targetCategoryName, ignoreCase = true) ||
                            it.name.lowercase().contains(targetCategoryName.lowercase().substringBefore("&").trim())
                }
                if (matched != null) return matched
            }
        }

        // 3. Substring fallback across keyword map
        for ((targetCategoryName, keywords) in keywordCategoryMap) {
            if (keywords.any { keyword -> normalized.contains(keyword) }) {
                val matched = availableCategories.find {
                    it.name.equals(targetCategoryName, ignoreCase = true) ||
                            it.name.lowercase().contains(targetCategoryName.lowercase().substringBefore("&").trim())
                }
                if (matched != null) return matched
            }
        }

        return null
    }

    fun suggestCategoryName(text: String): String? {
        if (text.isBlank()) return null
        val normalized = text.lowercase()

        for ((targetCategoryName, keywords) in keywordCategoryMap) {
            if (keywords.any { keyword ->
                    val pattern = "\\b${Regex.escape(keyword)}\\b".toRegex()
                    pattern.containsMatchIn(normalized) || normalized.contains(keyword)
                }) {
                return targetCategoryName
            }
        }
        return null
    }
}
