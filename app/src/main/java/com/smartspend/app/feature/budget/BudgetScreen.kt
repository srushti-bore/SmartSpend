package com.smartspend.app.feature.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartspend.app.core.money.MoneyUtils
import com.smartspend.app.core.ui.components.AtelierPillBadge
import com.smartspend.app.core.ui.components.CircularGauge
import com.smartspend.app.core.ui.components.DoubleHairlineRule
import com.smartspend.app.core.ui.components.EmptyState
import com.smartspend.app.core.ui.components.HairlineDivider
import com.smartspend.app.core.ui.components.LedgerSealFooter
import com.smartspend.app.core.ui.components.LoadingState
import com.smartspend.app.core.ui.components.SectionLabel
import com.smartspend.app.core.ui.theme.AtelierAmber
import com.smartspend.app.core.ui.theme.AtelierAmberSubtle
import com.smartspend.app.core.ui.theme.AtelierCanvas
import com.smartspend.app.core.ui.theme.AtelierCoral
import com.smartspend.app.core.ui.theme.AtelierCoralSubtle
import com.smartspend.app.core.ui.theme.AtelierHairline
import com.smartspend.app.core.ui.theme.AtelierInkMuted
import com.smartspend.app.core.ui.theme.AtelierPeriwinkle
import com.smartspend.app.core.ui.theme.AtelierPeriwinkleSubtle
import com.smartspend.app.core.ui.theme.AtelierPrimaryInk
import com.smartspend.app.core.ui.theme.AtelierSage
import com.smartspend.app.core.ui.theme.AtelierSageSubtle
import com.smartspend.app.core.ui.theme.AtelierSurfaceChalk
import com.smartspend.app.core.ui.theme.NewsreaderFontFamily
import com.smartspend.app.domain.model.BudgetProgress
import com.smartspend.app.domain.model.BudgetStatus
import com.smartspend.app.domain.model.BudgetType
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BudgetScreen(
    onNavigateBack: () -> Unit,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    val currentMonthStr = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())

    if (state.isAddDialogOpen) {
        AlertDialog(
            onDismissRequest = viewModel::closeAddDialog,
            containerColor = AtelierCanvas,
            title = {
                Text(
                    text = "Establish Budget Allocation",
                    fontFamily = NewsreaderFontFamily,
                    fontSize = 20.sp,
                    color = AtelierPrimaryInk
                )
            },
            text = {
                Column {
                    SectionLabel(text = "Allocation Frequency")
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            BudgetType.DAILY,
                            BudgetType.WEEKLY,
                            BudgetType.MONTHLY,
                            BudgetType.CATEGORY,
                            BudgetType.YEARLY
                        ).forEach { type ->
                            val isSelected = state.newBudgetType == type
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isSelected) AtelierPrimaryInk else AtelierSurfaceChalk,
                                modifier = Modifier
                                    .border(1.dp, if (isSelected) AtelierPrimaryInk else AtelierHairline, RoundedCornerShape(4.dp))
                                    .clickable { viewModel.onTypeSelect(type) }
                            ) {
                                Text(
                                    text = when (type) {
                                        BudgetType.DAILY -> "Daily"
                                        BudgetType.WEEKLY -> "Weekly"
                                        BudgetType.MONTHLY -> "Monthly"
                                        BudgetType.CATEGORY -> "Category"
                                        BudgetType.YEARLY -> "Yearly"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) AtelierCanvas else AtelierPrimaryInk,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    if (state.newBudgetType == BudgetType.CATEGORY) {
                        Spacer(modifier = Modifier.height(14.dp))
                        SectionLabel(text = "Select Category")
                        Spacer(modifier = Modifier.height(6.dp))
                        val selectedCategory = state.categories.find { it.id == state.selectedCategoryId }
                        Box {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, AtelierHairline, RoundedCornerShape(4.dp))
                                    .clickable { categoryDropdownExpanded = true },
                                shape = RoundedCornerShape(4.dp),
                                color = AtelierSurfaceChalk
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedCategory?.name ?: "Choose Category",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                        color = AtelierPrimaryInk
                                    )
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = null,
                                        tint = AtelierInkMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = categoryDropdownExpanded,
                                onDismissRequest = { categoryDropdownExpanded = false }
                            ) {
                                state.categories.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category.name) },
                                        onClick = {
                                            viewModel.onCategorySelect(category.id)
                                            categoryDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SectionLabel(text = "Fiscal Cap Amount (${state.preferredCurrency})")
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = state.newBudgetAmountInput,
                        onValueChange = viewModel::onAmountInputChange,
                        placeholder = { Text("e.g. 500.00") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp)
                    )

                    if (state.errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.errorMessage ?: "",
                            color = AtelierCoral,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = viewModel::saveBudget,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AtelierPrimaryInk,
                        contentColor = AtelierCanvas
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Register Budget")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::closeAddDialog) {
                    Text("Cancel", color = AtelierInkMuted)
                }
            }
        )
    }

    Scaffold(
        containerColor = AtelierCanvas,
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::openAddDialog,
                containerColor = AtelierAmber,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 3.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Budget")
            }
        }
    ) { paddingValues ->
        if (state.isLoading) {
            LoadingState(message = "Reading Budget Allocations...")
        } else {
            val filteredBudgets = if (state.selectedFilterType != null) {
                state.budgetProgressList.filter { it.budget.type == state.selectedFilterType }
            } else {
                state.budgetProgressList
            }

            val overallMonthlyBudget = state.budgetProgressList.firstOrNull { it.budget.type == BudgetType.MONTHLY && it.budget.categoryId == null }
            val overallDailyBudget = state.budgetProgressList.firstOrNull { it.budget.type == BudgetType.DAILY && it.budget.categoryId == null }
            val categoryBudgetsList = state.budgetProgressList.filter { it.budget.type == BudgetType.CATEGORY || (it.budget.type == BudgetType.MONTHLY && it.budget.categoryId != null) }
            val overallWeeklyBudget = state.budgetProgressList.firstOrNull { it.budget.type == BudgetType.WEEKLY && it.budget.categoryId == null }

            val (totalLimit, totalSpent, periodLabel) = if (state.selectedFilterType != null) {
                val spent = filteredBudgets.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.spentAmount) }
                val limit = filteredBudgets.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.budget.amount) }
                val label = when (state.selectedFilterType) {
                    BudgetType.DAILY -> "Daily Expenditure Limit"
                    BudgetType.WEEKLY -> "Weekly Expenditure Limit"
                    BudgetType.MONTHLY -> "Monthly Expenditure Limit"
                    BudgetType.CATEGORY -> "Category Expenditure Limits"
                    BudgetType.YEARLY -> "Yearly Expenditure Limit"
                    else -> "Expenditure Limit"
                }
                Triple(limit, spent, label)
            } else {
                // When "All" is active, prioritize the broader Monthly Fiscal Envelope instead of blindly adding Daily + Monthly
                when {
                    overallMonthlyBudget != null -> Triple(
                        overallMonthlyBudget.budget.amount,
                        overallMonthlyBudget.spentAmount,
                        "Monthly Expenditure Limit"
                    )
                    categoryBudgetsList.isNotEmpty() -> Triple(
                        categoryBudgetsList.fold(BigDecimal.ZERO) { acc, it -> acc.add(it.budget.amount) },
                        categoryBudgetsList.fold(BigDecimal.ZERO) { acc, it -> acc.add(it.spentAmount) },
                        "Category Budgets Combined"
                    )
                    overallWeeklyBudget != null -> Triple(
                        overallWeeklyBudget.budget.amount,
                        overallWeeklyBudget.spentAmount,
                        "Weekly Expenditure Limit"
                    )
                    overallDailyBudget != null -> Triple(
                        overallDailyBudget.budget.amount,
                        overallDailyBudget.spentAmount,
                        "Daily Expenditure Limit"
                    )
                    else -> Triple(
                        state.budgetProgressList.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.budget.amount) },
                        state.budgetProgressList.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.spentAmount) },
                        "Total Expenditure Limit"
                    )
                }
            }

            val totalPercentage = if (totalLimit > BigDecimal.ZERO) {
                totalSpent.multiply(BigDecimal(100)).divide(totalLimit, 0, java.math.RoundingMode.HALF_EVEN).toInt()
            } else 0
            val unallocated = (totalLimit - totalSpent).coerceAtLeast(BigDecimal.ZERO)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AtelierCanvas)
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp)
            ) {
                // 1. TOP HEADER & NAVIGATION
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = AtelierPrimaryInk,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(9999.dp),
                            color = AtelierSurfaceChalk,
                            modifier = Modifier.border(1.dp, AtelierHairline, RoundedCornerShape(9999.dp))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = "Previous Month",
                                    tint = AtelierInkMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = currentMonthStr,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = AtelierPrimaryInk
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Next Month",
                                    tint = AtelierInkMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    HairlineDivider()
                }

                // 2. ALLOCATION REGISTER SUB-HEADER
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 18.dp, bottom = 16.dp)
                    ) {
                        SectionLabel(text = "Allocation Register")
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Budgets & Limits",
                            fontFamily = NewsreaderFontFamily,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Medium,
                            color = AtelierPrimaryInk,
                            letterSpacing = (-0.5).sp
                        )
                    }
                }

                // 3. MASTER BUDGET SUMMARY SHELF
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AtelierHairline, RoundedCornerShape(4.dp)),
                        shape = RoundedCornerShape(4.dp),
                        color = AtelierSurfaceChalk.copy(alpha = 0.6f)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SectionLabel(text = periodLabel)
                                AtelierPillBadge(
                                    text = "${MoneyUtils.format(unallocated, state.preferredCurrency)} Remaining",
                                    backgroundColor = AtelierSageSubtle,
                                    contentColor = AtelierSage
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = MoneyUtils.format(totalSpent, state.preferredCurrency),
                                    fontFamily = NewsreaderFontFamily,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AtelierPrimaryInk
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "of ${MoneyUtils.format(totalLimit, state.preferredCurrency)} limit",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AtelierInkMuted
                                )
                            }

                            if (state.selectedFilterType == null && overallDailyBudget != null && overallMonthlyBudget != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "• Includes Daily Pacing Limit of ${MoneyUtils.format(overallDailyBudget.budget.amount, state.preferredCurrency)}/day (${MoneyUtils.format(overallDailyBudget.remainingAmount.coerceAtLeast(BigDecimal.ZERO), state.preferredCurrency)} left today)",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = AtelierInkMuted
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            LinearProgressIndicator(
                                progress = (totalPercentage / 100f).coerceIn(0f, 1f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (totalPercentage >= 90) AtelierCoral else if (totalPercentage >= 75) AtelierAmber else AtelierSage,
                                trackColor = AtelierSurfaceChalk
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Fiscal Cap Target",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = AtelierInkMuted
                                )
                                Text(
                                    text = "$totalPercentage% utilized",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = AtelierPrimaryInk
                                )
                            }
                        }
                    }
                }

                // 4. PERIOD FILTER CHIPS
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val filterOptions = listOf(
                            "All" to null,
                            "Daily" to BudgetType.DAILY,
                            "Weekly" to BudgetType.WEEKLY,
                            "Monthly" to BudgetType.MONTHLY,
                            "Category" to BudgetType.CATEGORY
                        )

                        filterOptions.forEach { (label, type) ->
                            val isSelected = state.selectedFilterType == type
                            Surface(
                                shape = RoundedCornerShape(9999.dp),
                                color = if (isSelected) AtelierPrimaryInk else AtelierSurfaceChalk,
                                modifier = Modifier
                                    .border(
                                        1.dp,
                                        if (isSelected) AtelierPrimaryInk else AtelierHairline,
                                        RoundedCornerShape(9999.dp)
                                    )
                                    .clickable { viewModel.onFilterSelect(type) }
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) AtelierCanvas else AtelierPrimaryInk,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                    DoubleHairlineRule(modifier = Modifier.padding(bottom = 14.dp))
                }

                // 5. BUDGET ALLOCATIONS LIST
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val headerTitle = when (state.selectedFilterType) {
                            BudgetType.DAILY -> "Daily Spending Ceilings"
                            BudgetType.WEEKLY -> "Weekly Spending Ceilings"
                            BudgetType.MONTHLY -> "Monthly Spending Ceilings"
                            BudgetType.CATEGORY -> "Category Spending Ceilings"
                            BudgetType.YEARLY -> "Yearly Spending Ceilings"
                            null -> "Active Allocations"
                        }
                        SectionLabel(text = "$headerTitle (${filteredBudgets.size})")
                    }
                }

                // Empty State or List
                if (filteredBudgets.isEmpty()) {
                    item {
                        EmptyState(
                            title = "No Budget Allocations",
                            description = "Define spending ceilings for daily, weekly, monthly, or category expenses.",
                            actionButtonText = "Establish Budget Limit",
                            onActionClick = viewModel::openAddDialog
                        )
                    }
                } else {
                    itemsIndexed(filteredBudgets) { index, progress ->
                        val category = state.categories.find { it.id == progress.budget.categoryId }
                        val categoryName = category?.name
                            ?: when (progress.budget.type) {
                                BudgetType.DAILY -> "Daily Limit"
                                BudgetType.WEEKLY -> "Weekly Limit"
                                BudgetType.MONTHLY -> "Monthly Limit"
                                BudgetType.CATEGORY -> "Category Limit"
                                BudgetType.YEARLY -> "Yearly Limit"
                            }
                        val folioNumber = "FOLIO DIV-${String.format("%02d", index + 1)}"

                        BudgetItemRow(
                            progress = progress,
                            categoryName = categoryName,
                            folioNumber = folioNumber,
                            preferredCurrency = state.preferredCurrency,
                            onDelete = { viewModel.deleteBudget(progress.budget.id) }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                // 6. BOTTOM ACTIONS & SEAL
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TextButton(onClick = viewModel::openAddDialog) {
                            Text(
                                text = "+ Create New Budget Allocation",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AtelierPeriwinkle
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        LedgerSealFooter(text = "Reconciled Allocation Register")
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetItemRow(
    progress: BudgetProgress,
    categoryName: String,
    folioNumber: String,
    preferredCurrency: String,
    onDelete: () -> Unit
) {
    val statusColor = when (progress.status) {
        BudgetStatus.ON_TRACK -> AtelierSage
        BudgetStatus.NEAR_LIMIT -> AtelierAmber
        BudgetStatus.OVER_BUDGET -> AtelierCoral
    }

    val statusBg = when (progress.status) {
        BudgetStatus.ON_TRACK -> AtelierSageSubtle
        BudgetStatus.NEAR_LIMIT -> AtelierAmberSubtle
        BudgetStatus.OVER_BUDGET -> AtelierCoralSubtle
    }

    val statusText = when (progress.status) {
        BudgetStatus.ON_TRACK -> "On track"
        BudgetStatus.NEAR_LIMIT -> "Near limit"
        BudgetStatus.OVER_BUDGET -> "Over limit"
    }

    val typeBadgeText = when (progress.budget.type) {
        BudgetType.DAILY -> "DAILY"
        BudgetType.WEEKLY -> "WEEKLY"
        BudgetType.MONTHLY -> "MONTHLY"
        BudgetType.CATEGORY -> "CATEGORY"
        BudgetType.YEARLY -> "YEARLY"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AtelierHairline, RoundedCornerShape(4.dp)),
        shape = RoundedCornerShape(4.dp),
        color = AtelierCanvas
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular Progress Gauge
            CircularGauge(
                progress = (progress.percentageUsed / 100f).coerceIn(0f, 1f),
                size = 54.dp,
                strokeWidth = 4.dp,
                trackColor = AtelierSurfaceChalk,
                progressColor = statusColor
            ) {
                Text(
                    text = "${progress.percentageUsed}%",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AtelierPrimaryInk
                    )
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = AtelierPrimaryInk,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(2.dp),
                        color = AtelierSurfaceChalk
                    ) {
                        Text(
                            text = typeBadgeText,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                            color = AtelierInkMuted,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$folioNumber • Registered Ceiling",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = AtelierInkMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = MoneyUtils.format(progress.spentAmount, preferredCurrency),
                        fontFamily = NewsreaderFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = AtelierPrimaryInk
                    )
                    Text(
                        text = " of ${MoneyUtils.format(progress.budget.amount, preferredCurrency)} Limit",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = AtelierInkMuted
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Status chip and delete action
            Column(horizontalAlignment = Alignment.End) {
                AtelierPillBadge(
                    text = statusText,
                    backgroundColor = statusBg,
                    contentColor = statusColor
                )
                Spacer(modifier = Modifier.height(6.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Budget",
                        tint = AtelierInkMuted.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
