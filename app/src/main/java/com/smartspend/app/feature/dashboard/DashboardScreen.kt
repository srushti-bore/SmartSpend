package com.smartspend.app.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Tram
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartspend.app.core.money.MoneyUtils
import com.smartspend.app.core.ui.components.AtelierPillBadge
import com.smartspend.app.core.ui.components.CircularGauge
import com.smartspend.app.core.ui.components.DoubleHairlineRule
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
import com.smartspend.app.core.ui.theme.AtelierLavender
import com.smartspend.app.core.ui.theme.AtelierPeriwinkle
import com.smartspend.app.core.ui.theme.AtelierPeriwinkleSubtle
import com.smartspend.app.core.ui.theme.AtelierPrimaryInk
import com.smartspend.app.core.ui.theme.AtelierSage
import com.smartspend.app.core.ui.theme.AtelierSageSubtle
import com.smartspend.app.core.ui.theme.AtelierSurfaceChalk
import com.smartspend.app.core.ui.theme.NewsreaderFontFamily
import com.smartspend.app.domain.assisted.ParsedExpenseDraft
import com.smartspend.app.domain.model.Expense
import com.smartspend.app.feature.assisted.quickadd.QuickAddBottomSheet
import com.smartspend.app.feature.assisted.voice.VoiceExpenseBottomSheet
import com.smartspend.app.feature.export.ExportDialog
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
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
    onNavigateToSettings: () -> Unit = {},
    onLogout: () -> Unit = {},
    onAccountReset: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showQuickAddSheet by remember { mutableStateOf(false) }
    var showVoiceSheet by remember { mutableStateOf(false) }
    var showExportModal by remember { mutableStateOf(false) }
    var showProfileMenuSheet by remember { mutableStateOf(false) }
    var showAiDisabledDialog by remember { mutableStateOf(false) }
    val profileSheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToastMessage()
        }
    }

    if (state.isLoading) {
        LoadingState(message = "Reading General Ledger...")
        return
    }

    // AI Disabled Modal Dialog
    if (showAiDisabledDialog) {
        AlertDialog(
            onDismissRequest = { showAiDisabledDialog = false },
            containerColor = AtelierCanvas,
            title = {
                Text(
                    text = "AI Features Disabled",
                    fontFamily = NewsreaderFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = AtelierPrimaryInk
                )
            },
            text = {
                Text(
                    text = "SmartSpend AI features (BudgetBrain, conversational chat, leak hunting & spend forecasts) are currently disabled. Please enable AI and configure your Google Gemini API Key in Settings.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AtelierPrimaryInk
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAiDisabledDialog = false
                        onNavigateToSettings()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AtelierPrimaryInk,
                        contentColor = AtelierCanvas
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAiDisabledDialog = false }) {
                    Text("Cancel", color = AtelierInkMuted)
                }
            }
        )
    }

    val summary = state.summary
    val recentExpenses = summary?.recentExpenses?.take(5) ?: emptyList()
    val totalSpendMonth = summary?.totalSpentCurrentMonth ?: BigDecimal.ZERO
    val overallBudgetProgress = summary?.overallBudgetProgress
    val dailyBudgetProgress = summary?.dailyBudgetProgress

    val todayCal = java.time.LocalDate.now()
    val daysRemainingInMonth = (todayCal.lengthOfMonth() - todayCal.dayOfMonth + 1).coerceAtLeast(1)

    val (safeDaily, remainingLimitText, progressFraction) = when {
        dailyBudgetProgress != null -> {
            val safe = dailyBudgetProgress.remainingAmount
            val text = "Remaining of ${MoneyUtils.format(dailyBudgetProgress.budget.amount, state.preferredCurrency)} daily limit"
            val fraction = (1f - (dailyBudgetProgress.percentageUsed / 100f)).coerceIn(0f, 1f)
            Triple(safe, text, fraction)
        }
        overallBudgetProgress != null -> {
            val safe = (overallBudgetProgress.remainingAmount.divide(BigDecimal(daysRemainingInMonth), 2, java.math.RoundingMode.HALF_EVEN)).max(BigDecimal.ZERO)
            val text = "Pacing ($daysRemainingInMonth days left) of ${MoneyUtils.format(overallBudgetProgress.budget.amount, state.preferredCurrency)} limit"
            val fraction = (1f - (overallBudgetProgress.percentageUsed / 100f)).coerceIn(0f, 1f)
            Triple(safe, text, fraction)
        }
        else -> {
            Triple(
                BigDecimal.ZERO,
                "No budget ceiling set • Tap Budgets to create",
                1f
            )
        }
    }

    // Today's Date Formatted for Editorial Ribbon
    val todayDateFormatted = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = AtelierCanvas
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AtelierCanvas)
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // 1. TOP APP BAR HEADER
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(AtelierPrimaryInk),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = "SmartSpend Logo",
                                tint = AtelierCanvas,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "SmartSpend",
                            fontFamily = NewsreaderFontFamily,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Medium,
                            color = AtelierPrimaryInk,
                            letterSpacing = (-0.5).sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = {
                                if (state.isAiEnabled) {
                                    onNavigateToIntelligenceHub()
                                } else {
                                    showAiDisabledDialog = true
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Intelligence",
                                tint = if (state.isAiEnabled) AtelierLavender else AtelierInkMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onNavigateToAccounts,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = "Accounts",
                                tint = AtelierInkMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // User initials avatar monogram
                        val monogram = if (state.profileName.isNotBlank()) {
                            state.profileName.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")
                        } else "AK"

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(AtelierSurfaceChalk)
                                .border(1.dp, AtelierHairline, CircleShape)
                                .clickable { showProfileMenuSheet = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = monogram,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = AtelierPrimaryInk
                            )
                        }
                    }
                }
                HairlineDivider()
            }

            // 2. SCREEN SUB-HEADER & DATE RIBBON
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        SectionLabel(text = todayDateFormatted)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Daily Ledger",
                            fontFamily = NewsreaderFontFamily,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Medium,
                            color = AtelierPrimaryInk,
                            letterSpacing = (-0.5).sp
                        )
                    }

                    AtelierPillBadge(
                        text = "Reconciled 08:30 AM",
                        icon = Icons.Default.VerifiedUser,
                        backgroundColor = AtelierSageSubtle,
                        contentColor = AtelierSage
                    )
                }
                HairlineDivider()
            }

            // 3. MONUMENTAL SAFE-TO-SPEND FOCAL SECTION
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularGauge(
                        progress = progressFraction,
                        size = 230.dp,
                        strokeWidth = 6.dp,
                        trackColor = AtelierSurfaceChalk,
                        progressColor = AtelierAmber
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            SectionLabel(text = "Daily Safe-to-Spend")
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = MoneyUtils.format(safeDaily, state.preferredCurrency),
                                fontFamily = NewsreaderFontFamily,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Normal,
                                color = AtelierPrimaryInk,
                                letterSpacing = (-1).sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = remainingLimitText,
                                style = MaterialTheme.typography.bodySmall,
                                color = AtelierInkMuted
                            )
                        }
                    }

                    // Ratio Metadata Shelf
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp)
                            .border(1.dp, AtelierHairline, RoundedCornerShape(2.dp))
                            .background(AtelierSurfaceChalk.copy(alpha = 0.4f))
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            SectionLabel(text = "Today's Debits")
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = MoneyUtils.format(state.todayDebits, state.preferredCurrency),
                                    fontFamily = NewsreaderFontFamily,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = AtelierPrimaryInk
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "DR",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = AtelierInkMuted
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .width(1.dp)
                                .background(AtelierHairline)
                        )

                        Column(horizontalAlignment = Alignment.End) {
                            SectionLabel(text = "Carryover Surplus")
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "+${MoneyUtils.format(state.carryoverSurplus, state.preferredCurrency)}",
                                    fontFamily = NewsreaderFontFamily,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = AtelierSage
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "CR",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = AtelierSage
                                )
                            }
                        }
                    }
                }
            }

            // 4. DOUBLE HAIRLINE SEPARATOR
            item {
                DoubleHairlineRule(modifier = Modifier.padding(vertical = 12.dp))
            }

            // 5. SEVEN-DAY DISBURSEMENT RHYTHM
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionLabel(text = "Seven-Day Disbursement Rhythm")
                        Surface(
                            shape = RoundedCornerShape(9999.dp),
                            color = AtelierPeriwinkleSubtle
                        ) {
                            Text(
                                text = "Avg. ${MoneyUtils.format(state.sevenDayAvgDebit, state.preferredCurrency)}/day",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = AtelierPeriwinkle
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 7-day Bar Chart Card
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AtelierHairline, RoundedCornerShape(4.dp)),
                        shape = RoundedCornerShape(4.dp),
                        color = AtelierSurfaceChalk.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val days = if (state.sevenDayRhythm.isNotEmpty()) {
                                val maxAmt = state.sevenDayRhythm.maxOfOrNull { it.second } ?: BigDecimal.ONE
                                val safeMax = if (maxAmt > BigDecimal.ZERO) maxAmt else BigDecimal.ONE
                                state.sevenDayRhythm.map { (label, amt) ->
                                    val frac = (amt.toFloat() / safeMax.toFloat()).coerceIn(0.08f, 1.0f)
                                    Triple(label, frac, MoneyUtils.format(amt, state.preferredCurrency))
                                }
                            } else {
                                listOf(
                                    Triple("18 F", 0.60f, MoneyUtils.format(BigDecimal("45"), state.preferredCurrency)),
                                    Triple("19 S", 0.85f, MoneyUtils.format(BigDecimal("62"), state.preferredCurrency)),
                                    Triple("20 S", 0.38f, MoneyUtils.format(BigDecimal("28"), state.preferredCurrency)),
                                    Triple("21 M", 0.48f, MoneyUtils.format(BigDecimal("35"), state.preferredCurrency)),
                                    Triple("22 T", 0.72f, MoneyUtils.format(BigDecimal("52"), state.preferredCurrency)),
                                    Triple("23 W", 0.42f, MoneyUtils.format(BigDecimal("31"), state.preferredCurrency)),
                                    Triple("24 T", 0.34f, MoneyUtils.format(BigDecimal("22"), state.preferredCurrency))
                                )
                            }

                            days.forEachIndexed { index, (label, heightFraction, amt) ->
                                val isToday = index == days.size - 1
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.fillMaxHeight()
                                ) {
                                    Text(
                                        text = amt,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isToday) AtelierAmber else AtelierInkMuted
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(22.dp)
                                            .fillMaxHeight(heightFraction)
                                            .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                            .background(if (isToday) AtelierAmber else AtelierPeriwinkle.copy(alpha = 0.7f))
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isToday) AtelierAmber else AtelierInkMuted
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 6. RECENT VOUCHERS TABLE
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Vouchers",
                            fontFamily = NewsreaderFontFamily,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Medium,
                            color = AtelierPrimaryInk
                        )
                        Surface(
                            shape = RoundedCornerShape(2.dp),
                            color = AtelierSurfaceChalk
                        ) {
                            Text(
                                text = "LEDGER FOLIO NO. 412",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    letterSpacing = 0.08.sp,
                                    color = AtelierInkMuted
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Column Headers
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1.2f)) {
                            SectionLabel(text = "Particulars")
                        }
                        Row(modifier = Modifier.weight(0.9f)) {
                            SectionLabel(text = "Audit Status")
                        }
                        Row(modifier = Modifier.weight(0.9f), horizontalArrangement = Arrangement.End) {
                            SectionLabel(text = "Disbursement")
                        }
                    }
                    HairlineDivider()
                }
            }

            // Recent Expenses List
            if (recentExpenses.isEmpty()) {
                item {
                    // Default Sample Voucher rows matching the Stitch mockup if no expenses yet
                    VoucherRow(
                        merchant = "Artisan Roast Works",
                        meta = "08:14 AM • Food & Provisions",
                        statusText = "On track",
                        statusColor = AtelierSage,
                        amount = "↓ ${MoneyUtils.format(BigDecimal("4.75"), state.preferredCurrency)}",
                        onClick = onNavigateToLedger
                    )
                    VoucherRow(
                        merchant = "Metropolitan Transit Rail",
                        meta = "09:05 AM • Commute & Freight",
                        statusText = "On track",
                        statusColor = AtelierSage,
                        amount = "↓ ${MoneyUtils.format(BigDecimal("2.75"), state.preferredCurrency)}",
                        onClick = onNavigateToLedger
                    )
                    VoucherRow(
                        merchant = "Merchant Stationery Co.",
                        meta = "01:20 PM • Office Supplies",
                        statusText = "Near limit",
                        statusColor = AtelierAmber,
                        amount = "↓ ${MoneyUtils.format(BigDecimal("15.00"), state.preferredCurrency)}",
                        onClick = onNavigateToLedger
                    )
                }
            } else {
                items(recentExpenses) { expense ->
                    val dateFormatted = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(expense.date))
                    val noteText = expense.notes ?: "Disbursement"
                    VoucherRow(
                        merchant = expense.title,
                        meta = "$dateFormatted • $noteText",
                        statusText = "On track",
                        statusColor = AtelierSage,
                        amount = "↓ ${MoneyUtils.format(expense.amount, expense.currency)}",
                        onClick = onNavigateToLedger
                    )
                }
            }

            // 7. FOOTING & HISTORICAL REPERTOIRE LINK
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 40.dp)
                ) {
                    DoubleHairlineRule()
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Showing 3 of 18 records this fiscal period",
                            style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                            color = AtelierInkMuted
                        )
                        Text(
                            text = "Inspect Historical Repertoire →",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = AtelierPeriwinkle
                            ),
                            modifier = Modifier
                                .clickable { onNavigateToLedger() }
                                .padding(4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    LedgerSealFooter()
                }
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

    if (showExportModal || state.isExportDialogOpen) {
        ExportDialog(
            profileId = state.activeProfileId,
            exportTransactionsUseCase = viewModel.exportTransactionsUseCase,
            onDismiss = {
                showExportModal = false
            }
        )
    }

    if (showProfileMenuSheet) {
        com.smartspend.app.feature.profile.ProfileMenuBottomSheet(
            sheetState = profileSheetState,
            onDismiss = { showProfileMenuSheet = false },
            onLogout = onLogout,
            onAccountReset = onAccountReset,
            onNavigateToBackup = onNavigateToBackupRestore
        )
    }
}

@Composable
private fun VoucherRow(
    merchant: String,
    meta: String,
    statusText: String,
    statusColor: Color,
    amount: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Merchant details
        Column(modifier = Modifier.weight(1.2f)) {
            Text(
                text = merchant,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                color = AtelierPrimaryInk
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = meta,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = AtelierInkMuted
            )
        }

        // Audit status chip
        Row(
            modifier = Modifier.weight(0.9f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (statusColor == AtelierSage) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    color = statusColor,
                    fontWeight = FontWeight.Medium
                )
            )
        }

        // Amount in Newsreader font
        Row(
            modifier = Modifier.weight(0.9f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = amount,
                fontFamily = NewsreaderFontFamily,
                fontSize = 17.sp,
                fontWeight = FontWeight.Normal,
                color = AtelierPrimaryInk
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "DR",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = AtelierInkMuted
            )
        }
    }
    HairlineDivider()
}
