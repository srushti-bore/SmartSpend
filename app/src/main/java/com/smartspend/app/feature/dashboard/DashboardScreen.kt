package com.smartspend.app.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartspend.app.core.money.MoneyUtils
import com.smartspend.app.core.ui.components.BudgetProgressBar
import com.smartspend.app.core.ui.components.EmptyState
import com.smartspend.app.core.ui.components.ExpenseItemCard
import com.smartspend.app.core.ui.components.LoadingState
import com.smartspend.app.core.ui.components.MetricCard
import com.smartspend.app.core.ui.components.VisualMoodMascot
import com.smartspend.app.core.ui.theme.BrandAccent
import com.smartspend.app.core.ui.theme.BrandPrimary
import com.smartspend.app.core.ui.theme.PastelBlue
import com.smartspend.app.core.ui.theme.PastelGreen
import com.smartspend.app.core.ui.theme.PastelPink
import com.smartspend.app.core.ui.theme.PastelPurple
import com.smartspend.app.core.ui.theme.PastelYellow
import com.smartspend.app.core.ui.theme.StatusDanger
import com.smartspend.app.core.ui.theme.StatusSuccess
import com.smartspend.app.domain.assisted.ParsedExpenseDraft
import com.smartspend.app.domain.intelligence.SafeSpendTier
import com.smartspend.app.feature.assisted.quickadd.QuickAddBottomSheet
import com.smartspend.app.feature.assisted.voice.VoiceExpenseBottomSheet
import com.smartspend.app.feature.export.ExportDialog
import java.math.BigDecimal
import java.util.Locale

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect

@Composable
fun DashboardScreen(
    onNavigateToAddExpense: () -> Unit,
    onNavigateToLedger: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToIncome: () -> Unit,
    onNavigateToAccounts: () -> Unit,
    onNavigateToRecurring: () -> Unit,
    onNavigateToSavingsGoals: () -> Unit,
    onNavigateToReceiptScanner: () -> Unit,
    onNavigateToCsvImport: () -> Unit,
    onNavigateToIntelligenceHub: () -> Unit,
    onNavigateToAskSmartSpend: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToBackupRestore: () -> Unit,
    onNavigateToSplitExpense: () -> Unit,
    onOpenFullFormWithDraft: (ParsedExpenseDraft) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showQuickAddSheet by remember { mutableStateOf(false) }
    var showVoiceSheet by remember { mutableStateOf(false) }

    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToastMessage()
        }
    }

    if (state.isLoading) {
        LoadingState(message = "Loading your financial overview...")
        return
    }

    val summary = state.summary
    val cashFlow = state.cashFlow
    val streak = state.streakStatus

    // Determine mood tier based on budget usage
    val budgetPct = summary?.overallBudgetProgress?.percentageUsed ?: 0
    val moodTier = when {
        budgetPct >= 100 -> SafeSpendTier.DANGER
        budgetPct >= 80 -> SafeSpendTier.CAUTION
        budgetPct >= 50 -> SafeSpendTier.MODERATE
        else -> SafeSpendTier.HEALTHY
    }

    val remainingAmount = summary?.overallBudgetProgress?.remainingAmount ?: BigDecimal.ZERO
    val safeDailyStr = if (remainingAmount > BigDecimal.ZERO) {
        MoneyUtils.format(remainingAmount.divide(BigDecimal("20"), 2, java.math.RoundingMode.HALF_EVEN))
    } else "0.00"

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddExpense,
                containerColor = BrandPrimary,
                contentColor = Color.Black,
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            text = "SmartSpend AI Financial Command",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row {
                        IconButton(onClick = onNavigateToBackupRestore) {
                            Icon(Icons.Default.CloudSync, contentDescription = "Backup & Restore", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { viewModel.openExportDialog() }) {
                            Icon(Icons.Default.FileDownload, contentDescription = "Export CSV", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // Visual Mood Mascot & Financial AI Quick Launcher (Phase 4)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    VisualMoodMascot(
                        tier = moodTier,
                        percentageUsed = budgetPct,
                        safeDaily = safeDailyStr,
                        onMascotClick = onNavigateToIntelligenceHub
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            onClick = onNavigateToIntelligenceHub,
                            shape = RoundedCornerShape(12.dp),
                            color = BrandPrimary,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Psychology, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("AI Hub", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                            }
                        }

                        Surface(
                            onClick = onNavigateToAskSmartSpend,
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF6A11CB),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ask AI (मराठी/EN)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Streak & Habit Card (Phase 6)
            if (streak != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFFFF7ED), Color(0xFFFEF3C7))
                                )
                            )
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = if (streak.currentStreakDays >= 3) "🔥" else "⚡",
                                fontSize = 32.sp
                            )
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "${streak.currentStreakDays}-Day Logging Streak",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF9A3412)
                                    )
                                    if (streak.isLoggedToday) {
                                        Text("• Active Today", fontSize = 11.sp, color = Color(0xFF15803D), fontWeight = FontWeight.SemiBold)
                                    }
                                }
                                Text(
                                    text = streak.motivationalTip,
                                    fontSize = 12.sp,
                                    color = Color(0xFF78350F)
                                )
                            }
                        }
                    }
                }
            }

            // Assisted Fast Capture (Phase 3)
            item {
                Column {
                    Text(
                        text = "Assisted Fast Capture",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            onClick = { showQuickAddSheet = true },
                            shape = RoundedCornerShape(14.dp),
                            color = BrandPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Quick Add", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = BrandPrimary)
                            }
                        }

                        Surface(
                            onClick = { showVoiceSheet = true },
                            shape = RoundedCornerShape(14.dp),
                            color = PastelPurple.copy(alpha = 0.35f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Voice Add", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            }
                        }

                        Surface(
                            onClick = onNavigateToReceiptScanner,
                            shape = RoundedCornerShape(14.dp),
                            color = PastelBlue.copy(alpha = 0.35f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("OCR Scan", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }

            // Quick Hub Feature Navigation Bar (Phase 1 to 6)
            item {
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionPill(icon = Icons.Default.Bolt, label = "⚡ Demo Data", color = Color(0xFFFEF3C7), onClick = { viewModel.seedDemoData() })
                    QuickActionPill(icon = Icons.Default.Analytics, label = "Reports", color = Color(0xFFE0E7FF), onClick = onNavigateToReports)
                    QuickActionPill(icon = Icons.Default.CallSplit, label = "Split Bill", color = Color(0xFFDCFCE7), onClick = onNavigateToSplitExpense)
                    QuickActionPill(icon = Icons.Default.TrendingUp, label = "Income", color = PastelGreen, onClick = onNavigateToIncome)
                    QuickActionPill(icon = Icons.Default.AccountBalanceWallet, label = "Wallets", color = PastelBlue, onClick = onNavigateToAccounts)
                    QuickActionPill(icon = Icons.Default.Autorenew, label = "Subscriptions", color = PastelPurple, onClick = onNavigateToRecurring)
                    QuickActionPill(icon = Icons.Default.Savings, label = "Goals", color = PastelYellow, onClick = onNavigateToSavingsGoals)
                    QuickActionPill(icon = Icons.Default.PieChart, label = "Budgets", color = PastelPink, onClick = onNavigateToBudgets)
                    QuickActionPill(icon = Icons.Default.Category, label = "Categories", color = BrandAccent.copy(alpha = 0.25f), onClick = onNavigateToCategories)
                    QuickActionPill(icon = Icons.Default.FileUpload, label = "Import CSV", color = MaterialTheme.colorScheme.surfaceVariant, onClick = onNavigateToCsvImport)
                    QuickActionPill(icon = Icons.Default.CloudSync, label = "Backup", color = Color(0xFFF3E8FF), onClick = onNavigateToBackupRestore)
                }
            }

            // Net Cash Flow Bento Card
            if (cashFlow != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onNavigateToIncome),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Monthly Net Cash Flow",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${String.format(Locale.getDefault(), "%.1f", cashFlow.savingsRatePct)}% Saved",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (cashFlow.netSavings >= BigDecimal.ZERO) StatusSuccess else StatusDanger
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = MoneyUtils.format(cashFlow.netSavings),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (cashFlow.netSavings >= BigDecimal.ZERO) StatusSuccess else StatusDanger
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Income: +${MoneyUtils.format(cashFlow.totalIncome)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = StatusSuccess
                                )
                                Text(
                                    text = "Spent: -${MoneyUtils.format(cashFlow.totalExpense)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = StatusDanger
                                )
                            }
                        }
                    }
                }
            }

            // Summary Metrics Row
            item {
                val totalSpent = summary?.totalSpentCurrentMonth ?: BigDecimal.ZERO
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
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        EmptyState(
                            title = "No expenses recorded yet",
                            description = "Tap the '+' button below or use Assisted Capture to log a transaction.",
                            actionButtonText = "Add First Expense",
                            onActionClick = onNavigateToAddExpense
                        )

                        Surface(
                            onClick = { viewModel.seedDemoData() },
                            shape = RoundedCornerShape(14.dp),
                            color = BrandPrimary.copy(alpha = 0.2f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (state.isSeedingData) "Populating Demo Data..." else "⚡ Populate Rich Demo Financial Records",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
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

    if (showQuickAddSheet) {
        QuickAddBottomSheet(
            onDismiss = { showQuickAddSheet = false },
            onOpenFullEditor = { draft ->
                showQuickAddSheet = false
                onOpenFullFormWithDraft(draft)
            }
        )
    }

    if (showVoiceSheet) {
        VoiceExpenseBottomSheet(
            onDismiss = { showVoiceSheet = false },
            onOpenFullEditor = { draft ->
                showVoiceSheet = false
                onOpenFullFormWithDraft(draft)
            }
        )
    }

    if (state.isExportDialogOpen && state.activeProfileId.isNotEmpty()) {
        ExportDialog(
            profileId = state.activeProfileId,
            exportTransactionsUseCase = viewModel.exportTransactionsUseCase,
            onDismiss = { viewModel.closeExportDialog() }
        )
    }
}

@Composable
fun QuickActionPill(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = color,
        modifier = Modifier.height(40.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurface)
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
