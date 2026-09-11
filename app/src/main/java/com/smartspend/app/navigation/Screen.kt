package com.smartspend.app.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Lock : Screen("lock")
    object Dashboard : Screen("dashboard")
    object Ledger : Screen("ledger")
    object AddExpense : Screen("add_expense")
    object EditExpense : Screen("edit_expense/{expenseId}") {
        fun createRoute(expenseId: String) = "edit_expense/$expenseId"
    }
    object Budgets : Screen("budgets")
    object Categories : Screen("categories")
}
