package com.smartspend.app.feature.expense

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartspend.app.core.ui.components.EmptyState
import com.smartspend.app.core.ui.components.ExpenseItemCard
import com.smartspend.app.core.ui.components.LoadingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditExpense: (String) -> Unit,
    viewModel: LedgerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction Ledger") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = { Text("Search transactions, notes...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.selectedCategoryId == null,
                    onClick = { viewModel.onCategoryFilterSelect(null) },
                    label = { Text("All Categories") }
                )
                state.categories.forEach { category ->
                    FilterChip(
                        selected = state.selectedCategoryId == category.id,
                        onClick = { viewModel.onCategoryFilterSelect(category.id) },
                        label = { Text(category.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sort Selector Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Sort:", style = MaterialTheme.typography.labelMedium)
                FilterChip(
                    selected = state.sortOrder == "DATE_DESC",
                    onClick = { viewModel.onSortChange("DATE_DESC") },
                    label = { Text("Newest") }
                )
                FilterChip(
                    selected = state.sortOrder == "AMOUNT_DESC",
                    onClick = { viewModel.onSortChange("AMOUNT_DESC") },
                    label = { Text("Highest Amount") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (state.isLoading) {
                LoadingState(message = "Filtering transactions...")
            } else if (state.expenses.isEmpty()) {
                EmptyState(
                    title = "No transactions found",
                    description = if (state.searchQuery.isNotEmpty() || state.selectedCategoryId != null) {
                        "Try changing or clearing your search / filter criteria."
                    } else {
                        "You haven't recorded any expenses yet."
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.expenses) { expense ->
                        val category = state.categoriesMap[expense.categoryId]
                        ExpenseItemCard(
                            expense = expense,
                            categoryName = category?.name ?: "Expense",
                            categoryColorHex = category?.colorHex ?: "#80B3FF",
                            onClick = { onNavigateToEditExpense(expense.id) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}
