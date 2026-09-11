package com.smartspend.app.feature.budget

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartspend.app.core.ui.components.BudgetProgressBar
import com.smartspend.app.core.ui.components.EmptyState
import com.smartspend.app.core.ui.components.LoadingState
import com.smartspend.app.core.ui.theme.StatusDanger
import com.smartspend.app.domain.model.BudgetType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    onNavigateBack: () -> Unit,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isAddDialogOpen) {
        AlertDialog(
            onDismissRequest = viewModel::closeAddDialog,
            title = { Text("Set New Budget") },
            text = {
                Column {
                    Text("Select Period:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(BudgetType.DAILY, BudgetType.WEEKLY, BudgetType.MONTHLY, BudgetType.YEARLY).forEach { type ->
                            FilterChip(
                                selected = state.newBudgetType == type,
                                onClick = { viewModel.onTypeSelect(type) },
                                label = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = state.newBudgetAmountInput,
                        onValueChange = viewModel::onAmountInputChange,
                        label = { Text("Budget Limit (INR)") },
                        placeholder = { Text("5000.00") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (state.errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = state.errorMessage ?: "", color = StatusDanger, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(onClick = viewModel::saveBudget) {
                    Text("Save Budget")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::closeAddDialog) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budgets & Limits") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::openAddDialog,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Set Budget")
            }
        }
    ) { paddingValues ->
        if (state.isLoading) {
            LoadingState(message = "Loading budgets...")
        } else if (state.budgetProgressList.isEmpty()) {
            EmptyState(
                title = "No active budgets",
                description = "Set your daily, weekly, or monthly budget to keep spending within guardrails.",
                actionButtonText = "Set a Budget",
                onActionClick = viewModel::openAddDialog,
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(state.budgetProgressList) { progress ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BudgetProgressBar(
                            progress = progress,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.deleteBudget(progress.budget.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Budget", tint = StatusDanger)
                        }
                    }
                }
            }
        }
    }
}
