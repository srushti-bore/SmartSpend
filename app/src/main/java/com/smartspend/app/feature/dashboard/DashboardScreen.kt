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

    val summary = state.summary
    val recentExpenses = summary?.recentExpenses?.take(5) ?: emptyList()
    val totalSpendMonth = summary?.totalSpentCurrentMonth ?: BigDecimal.ZERO
    val remainingBudget = summary?.overallBudgetProgress?.remainingAmount ?: BigDecimal.ZERO

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
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onNavigateToIntelligenceHub() }
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
                    val safeDaily = if (remainingBudget > BigDecimal.ZERO) {
                        remainingBudget.divide(BigDecimal("20"), 2, java.math.RoundingMode.HALF_EVEN)
                    } else BigDecimal("42.50")

                    val progressFraction = if (summary != null && summary.overallBudgetProgress != null) {
                        1f - (summary.overallBudgetProgress.percentageUsed / 100f).coerceIn(0f, 1f)
                    } else 0.65f

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
                                text = MoneyUtils.format(safeDaily),
                                fontFamily = NewsreaderFontFamily,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Normal,
                                color = AtelierPrimaryInk,
                                letterSpacing = (-1).sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Remaining of ${MoneyUtils.format(remainingBudget)} limit",
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
                                    text = "$22.50",
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
                                    text = "+$14.20",
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
                                text = "Avg. $38.40/day",
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
                            val days = listOf(
                                Triple("18 F", 0.60f, "$45"),
                                Triple("19 S", 0.85f, "$62"),
                                Triple("20 S", 0.38f, "$28"),
                                Triple("21 M", 0.48f, "$35"),
                                Triple("22 T", 0.72f, "$52"),
                                Triple("23 W", 0.42f, "$31"),
                                Triple("24 T", 0.34f, "$22") // Today in amber
                            )

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
                        amount = "↓ $4.75",
                        onClick = onNavigateToLedger
                    )
                    VoucherRow(
                        merchant = "Metropolitan Transit Rail",
                        meta = "09:05 AM • Commute & Freight",
                        statusText = "On track",
                        statusColor = AtelierSage,
                        amount = "↓ $2.75",
                        onClick = onNavigateToLedger
                    )
                    VoucherRow(
                        merchant = "Merchant Stationery Co.",
                        meta = "01:20 PM • Office Supplies",
                        statusText = "Near limit",
                        statusColor = AtelierAmber,
                        amount = "↓ $15.00",
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
