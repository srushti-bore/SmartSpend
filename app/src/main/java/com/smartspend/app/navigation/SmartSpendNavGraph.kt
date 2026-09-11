package com.smartspend.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.smartspend.app.feature.budget.BudgetScreen
import com.smartspend.app.feature.category.CategoryScreen
import com.smartspend.app.feature.dashboard.DashboardScreen
import com.smartspend.app.feature.expense.AddEditExpenseScreen
import com.smartspend.app.feature.expense.LedgerScreen
import com.smartspend.app.feature.onboarding.OnboardingScreen
import com.smartspend.app.feature.profile.LockScreen

@Composable
fun SmartSpendNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Lock.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Lock.route) {
            LockScreen(
                onUnlockSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Lock.route) { inclusive = true }
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
                }
            )
        }

        composable(Screen.AddExpense.route) {
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
    }
}
