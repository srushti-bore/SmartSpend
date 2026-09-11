package com.smartspend.app.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.smartspend.app.feature.account.AccountScreen
import com.smartspend.app.feature.assisted.importcsv.CsvImportScreen
import com.smartspend.app.feature.assisted.ocr.ReceiptScannerScreen
import com.smartspend.app.feature.backup.BackupRestoreScreen
import com.smartspend.app.feature.budget.BudgetScreen
import com.smartspend.app.feature.category.CategoryScreen
import com.smartspend.app.feature.dashboard.DashboardScreen
import com.smartspend.app.feature.expense.AddEditExpenseScreen
import com.smartspend.app.feature.expense.LedgerScreen
import com.smartspend.app.feature.income.IncomeScreen
import com.smartspend.app.feature.intelligence.AskSmartSpendScreen
import com.smartspend.app.feature.intelligence.IntelligenceHubScreen
import com.smartspend.app.feature.onboarding.OnboardingScreen
import com.smartspend.app.feature.profile.LockScreen
import com.smartspend.app.feature.recurring.RecurringExpenseScreen
import com.smartspend.app.feature.report.ReportsScreen
import com.smartspend.app.feature.savingsgoal.SavingsGoalScreen
import com.smartspend.app.feature.split.SplitExpenseScreen

@Composable
fun SmartSpendNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Lock.route,
    sharedImageUri: Uri? = null
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Lock.route) {
            LockScreen(
                onUnlockSuccess = {
                    if (sharedImageUri != null) {
                        navController.navigate(Screen.ReceiptScanner.route) {
                            popUpTo(Screen.Lock.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Lock.route) { inclusive = true }
                        }
                    }
                },
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Lock.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onOnboardingComplete = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToAddExpense = {
                    navController.navigate(Screen.AddExpense.route)
                },
                onNavigateToLedger = {
                    navController.navigate(Screen.Ledger.route)
                },
                onNavigateToBudgets = {
                    navController.navigate(Screen.Budgets.route)
                },
                onNavigateToCategories = {
                    navController.navigate(Screen.Categories.route)
                },
                onNavigateToIncome = {
                    navController.navigate(Screen.Income.route)
                },
                onNavigateToAccounts = {
                    navController.navigate(Screen.Accounts.route)
                },
                onNavigateToRecurring = {
                    navController.navigate(Screen.Recurring.route)
                },
                onNavigateToSavingsGoals = {
                    navController.navigate(Screen.SavingsGoals.route)
                },
                onNavigateToReceiptScanner = {
                    navController.navigate(Screen.ReceiptScanner.route)
                },
                onNavigateToCsvImport = {
                    navController.navigate(Screen.CsvImport.route)
                },
                onNavigateToIntelligenceHub = {
                    navController.navigate(Screen.IntelligenceHub.route)
                },
                onNavigateToAskSmartSpend = {
                    navController.navigate(Screen.AskSmartSpend.route)
                },
                onNavigateToReports = {
                    navController.navigate(Screen.Reports.route)
                },
                onNavigateToBackupRestore = {
                    navController.navigate(Screen.BackupRestore.route)
                },
                onNavigateToSplitExpense = {
                    navController.navigate(Screen.SplitExpense.route)
                },
                onOpenFullFormWithDraft = { draft ->
                    navController.navigate(
                        Screen.AddExpense.createRoute(
                            title = draft.title,
                            amount = draft.amount?.toPlainString(),
                            notes = draft.notes,
                            date = draft.date
                        )
                    )
                }
            )
        }

        composable(
            route = "add_expense?prefillTitle={prefillTitle}&prefillAmount={prefillAmount}&prefillNotes={prefillNotes}&prefillDate={prefillDate}",
            arguments = listOf(
                navArgument("prefillTitle") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("prefillAmount") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("prefillNotes") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("prefillDate") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) {
            AddEditExpenseScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditExpense.route,
            arguments = listOf(navArgument("expenseId") { type = NavType.StringType })
        ) {
            AddEditExpenseScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Ledger.route) {
            LedgerScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditExpense = { expenseId ->
                    navController.navigate(Screen.EditExpense.createRoute(expenseId))
                }
            )
        }

        composable(Screen.Budgets.route) {
            BudgetScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Categories.route) {
            CategoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Income.route) {
            IncomeScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Accounts.route) {
            AccountScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Recurring.route) {
            RecurringExpenseScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.SavingsGoals.route) {
            SavingsGoalScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ReceiptScanner.route) {
            ReceiptScannerScreen(
                onNavigateBack = { navController.popBackStack() },
                onExpenseSaved = {
                    navController.navigate(Screen.Ledger.route) {
                        popUpTo(Screen.Dashboard.route)
                    }
                },
                sharedImageUri = sharedImageUri
            )
        }

        composable(Screen.CsvImport.route) {
            CsvImportScreen(
                onNavigateBack = { navController.popBackStack() },
                onImportComplete = {
                    navController.navigate(Screen.Ledger.route) {
                        popUpTo(Screen.Dashboard.route)
                    }
                }
            )
        }

        composable(Screen.IntelligenceHub.route) {
            IntelligenceHubScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAskAi = {
                    navController.navigate(Screen.AskSmartSpend.route)
                }
            )
        }

        composable(Screen.AskSmartSpend.route) {
            AskSmartSpendScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Reports.route) {
            ReportsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.BackupRestore.route) {
            BackupRestoreScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.SplitExpense.route) {
            SplitExpenseScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
