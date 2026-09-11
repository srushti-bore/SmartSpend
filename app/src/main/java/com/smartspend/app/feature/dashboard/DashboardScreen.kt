package com.smartspend.app.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartspend.app.core.money.MoneyUtils
import com.smartspend.app.core.ui.components.BudgetProgressBar
import com.smartspend.app.core.ui.components.EmptyState
import com.smartspend.app.core.ui.components.ExpenseItemCard
import com.smartspend.app.core.ui.components.LoadingState
import com.smartspend.app.core.ui.components.MetricCard
import com.smartspend.app.core.ui.theme.BrandAccent
import com.smartspend.app.core.ui.theme.BrandPrimary
import com.smartspend.app.core.ui.theme.PastelBlue
import com.smartspend.app.core.ui.theme.PastelGreen
import com.smartspend.app.core.ui.theme.PastelPink

@Composable
fun DashboardScreen(
    onNavigateToAddExpense: () -> Unit,
    onNavigateToLedger: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    onNavigateToCategories: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) {
        LoadingState(message = "Loading your financial overview...")
        return
    }

    val summary = state.summary

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddExpense,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hello, ${state.profileName.ifEmpty { "User" }} 👋",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Here is your monthly summary",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row {
                        IconButton(onClick = onNavigateToBudgets) {
                            Icon(Icons.Default.PieChart, contentDescription = "Budgets", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = onNavigateToCategories) {
                            Icon(Icons.Default.Category, contentDescription = "Categories", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // Summary Metrics Row
            item {
                val totalSpent = summary?.totalSpentCurrentMonth ?: java.math.BigDecimal.ZERO
                val budget = summary?.overallBudgetProgress

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "This Month's Spend",
                        value = MoneyUtils.format(totalSpent),
                        backgroundColor = PastelPink.copy(alpha = 0.35f),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Budget Remaining",
                        value = if (budget != null) MoneyUtils.format(budget.remainingAmount) else "No Budget",
                        subtitle = if (budget != null) "${budget.percentageUsed}% used" else "Tap to set",
                        backgroundColor = PastelGreen.copy(alpha = 0.35f),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Budget Progress Bar
            if (summary?.overallBudgetProgress != null) {
                item {
                    BudgetProgressBar(progress = summary.overallBudgetProgress)
                }
            }

            // Category Breakdown Section
            if (summary != null && summary.categoryBreakdown.isNotEmpty()) {
                item {
                    Text(
                        text = "Top Spending Categories",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        summary.categoryBreakdown.take(4).forEach { cat ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 1.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = cat.categoryName,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                    )
                                    Text(
                                        text = MoneyUtils.format(cat.totalAmount),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Recent Expenses Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (summary != null && summary.recentExpenses.isNotEmpty()) {
                        TextButton(onClick = onNavigateToLedger) {
                            Text("View All")
                        }
                    }
                }
            }

            if (summary == null || summary.recentExpenses.isEmpty()) {
                item {
                    EmptyState(
                        title = "No expenses recorded yet",
                        description = "Tap the '+' button below to record your first transaction.",
                        actionButtonText = "Add First Expense",
                        onActionClick = onNavigateToAddExpense
                    )
                }
            } else {
                items(summary.recentExpenses) { expense ->
                    val category = state.categoriesMap[expense.categoryId]
                    ExpenseItemCard(
                        expense = expense,
                        categoryName = category?.name ?: "Expense",
                        categoryColorHex = category?.colorHex ?: "#80B3FF"
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
