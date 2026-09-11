package com.smartspend.app.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Lock : Screen("lock")
    object Dashboard : Screen("dashboard")
    object Ledger : Screen("ledger")
    object AddExpense : Screen("add_expense?prefillTitle={prefillTitle}&prefillAmount={prefillAmount}&prefillNotes={prefillNotes}&prefillDate={prefillDate}") {
        fun createRoute(
            title: String? = null,
            amount: String? = null,
            notes: String? = null,
            date: Long? = null
        ): String {
            val params = mutableListOf<String>()
            if (!title.isNullOrBlank()) params.add("prefillTitle=$title")
            if (!amount.isNullOrBlank()) params.add("prefillAmount=$amount")
            if (!notes.isNullOrBlank()) params.add("prefillNotes=$notes")
            if (date != null && date > 0) params.add("prefillDate=$date")
            return if (params.isEmpty()) "add_expense" else "add_expense?${params.joinToString("&")}"
        }
    }
    object EditExpense : Screen("edit_expense/{expenseId}") {
        fun createRoute(expenseId: String) = "edit_expense/$expenseId"
    }
    object Budgets : Screen("budgets")
    object Categories : Screen("categories")
    object Income : Screen("income")
    object Accounts : Screen("accounts")
    object Recurring : Screen("recurring")
    object SavingsGoals : Screen("savings_goals")
    object ReceiptScanner : Screen("receipt_scanner")
    object CsvImport : Screen("csv_import")
    object IntelligenceHub : Screen("intelligence_hub")
    object AskSmartSpend : Screen("ask_smartspend")
    object Reports : Screen("reports")
    object BackupRestore : Screen("backup_restore")
    object SplitExpense : Screen("split_expense")
}
