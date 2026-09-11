package com.smartspend.app.feature.expense

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartspend.app.core.money.MoneyUtils
import com.smartspend.app.core.ui.components.AtelierPillBadge
import com.smartspend.app.core.ui.components.DoubleHairlineRule
import com.smartspend.app.core.ui.components.EmptyState
import com.smartspend.app.core.ui.components.HairlineDivider
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
import com.smartspend.app.core.ui.theme.IBMPlexSansFontFamily
import com.smartspend.app.core.ui.theme.NewsreaderFontFamily
import com.smartspend.app.domain.model.Expense
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LedgerScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditExpense: (String) -> Unit,
    viewModel: LedgerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var activeFilterTab by remember { mutableStateOf("All Entries") }

    Scaffold(
        containerColor = AtelierCanvas
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AtelierCanvas)
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            // 1. TOP HEADER
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
                        modifier = Modifier.clickable { onNavigateBack() }
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = AtelierPrimaryInk,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SmartSpend",
                            fontFamily = NewsreaderFontFamily,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Medium,
                            color = AtelierPrimaryInk
                        )
                    }

                    IconButton(
                        onClick = {},
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = "Account",
                            tint = AtelierInkMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                HairlineDivider()
            }

            // 2. PASSBOOK REGISTER TITLE
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp, bottom = 14.dp)
                ) {
                    SectionLabel(text = "Passbook Register • Vol. IX")
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "General Ledger",
                            fontFamily = NewsreaderFontFamily,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Medium,
                            color = AtelierPrimaryInk,
                            letterSpacing = (-0.5).sp
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(AtelierPeriwinkle)
                            )
                            Text(
                                text = "Reconciled Today",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = AtelierInkMuted
                            )
                        }
                    }
                }
            }

            // 3. SEARCH REGISTER BAR
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AtelierHairline, RoundedCornerShape(4.dp)),
                    shape = RoundedCornerShape(4.dp),
                    color = AtelierSurfaceChalk.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = AtelierInkMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        BasicTextField(
                            value = state.searchQuery,
                            onValueChange = viewModel::onSearchQueryChange,
                            textStyle = TextStyle(
                                fontFamily = IBMPlexSansFontFamily,
                                fontSize = 13.sp,
                                color = AtelierPrimaryInk
                            ),
                            cursorBrush = SolidColor(AtelierAmber),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            decorationBox = { innerTextField ->
                                if (state.searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search memo, payee, or ledger voucher...",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 13.sp,
                                            color = AtelierInkMuted.copy(alpha = 0.6f)
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        )
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.onSearchQueryChange("") },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = AtelierInkMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Filter",
                                tint = AtelierInkMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 4. FILTER TABS ROW
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp, bottom = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    listOf("All Entries", "This Week", "This Month", "Categories", "Reconciled").forEach { tab ->
                        val isSelected = activeFilterTab == tab
                        Column(
                            modifier = Modifier
                                .clickable { activeFilterTab = tab }
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = tab,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) AtelierPrimaryInk else AtelierInkMuted
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .width(36.dp)
                                        .height(2.dp)
                                        .background(AtelierPrimaryInk)
                                )
                            } else {
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                        }
                    }
                }
                HairlineDivider()
            }

            // 5. SUMMARY BANNER (PASSBOOK METRIC SUMMARY)
            item {
                val totalDebit = state.expenses.fold(BigDecimal.ZERO) { acc, exp -> acc.add(exp.amount) }
                val entriesCount = state.expenses.size
                val dailyAvg = if (entriesCount > 0) {
                    totalDebit.divide(BigDecimal(entriesCount.coerceAtLeast(1)), 2, java.math.RoundingMode.HALF_EVEN)
                } else BigDecimal("47.33")

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .border(1.dp, AtelierHairline, RoundedCornerShape(4.dp)),
                    shape = RoundedCornerShape(4.dp),
                    color = AtelierSurfaceChalk.copy(alpha = 0.6f)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Month total out
                            Column(modifier = Modifier.weight(1f)) {
                                SectionLabel(text = "Month Total Out")
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = MoneyUtils.format(if (totalDebit > BigDecimal.ZERO) totalDebit else BigDecimal("1420.00")),
                                        fontFamily = NewsreaderFontFamily,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = AtelierCoral
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "↓ DR",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AtelierCoral
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$entriesCount debit entries recorded",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = AtelierInkMuted
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(50.dp)
                                    .background(AtelierHairline)
                            )

                            // Daily average
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 16.dp)
                            ) {
                                SectionLabel(text = "Daily Average")
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = MoneyUtils.format(dailyAvg),
                                        fontFamily = NewsreaderFontFamily,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = AtelierPrimaryInk
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "/ day",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            color = AtelierPeriwinkle
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "-12.4% vs. previous billing",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = AtelierInkMuted
                                )
                            }
                        }
                    }
                }
            }

            // 6. TRANSACTION ENTRIES: RULED KHATA LEDGER
            if (state.isLoading) {
                item {
                    LoadingState(message = "Compiling General Ledger...")
                }
            } else if (state.expenses.isEmpty()) {
                item {
                    // Render prototype mockup rows if empty
                    PrototypeLedgerDateGroup(
                        dateTitle = "Thursday, 24 October",
                        dayDebit = "$118.40",
                        entries = listOf(
                            PrototypeEntry("Blue Bottle Roasters", "Single origin drip & ledger…", "Logged", AtelierSage, "-$7.50 DR", "08:42 AM", Icons.Default.Coffee),
                            PrototypeEntry("Powell's Book Station", "Double-entry accounting…", "Split", AtelierPeriwinkle, "-$48.90 DR", "11:15 AM", Icons.Default.MenuBook),
                            PrototypeEntry("Shell Station Depot", "Unusual fuel surcharge…", "Flagged", AtelierCoral, "-$62.00 DR", "04:30 PM", Icons.Default.LocalGasStation)
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    PrototypeLedgerDateGroup(
                        dateTitle = "Wednesday, 23 October",
                        dayDebit = "$164.20",
                        entries = listOf(
                            PrototypeEntry("Merchant Provisions", "Pantry provisions, olive oil…", "Logged", AtelierAmber, "-$142.20 DR", "02:15 PM", Icons.Default.ShoppingBag),
                            PrototypeEntry("Heritage Bakery Co.", "Rye loaf & cardamom bun", "Logged", AtelierSage, "-$22.00 DR", "09:02 AM", Icons.Default.Coffee)
                        )
                    )
                }
            } else {
                // Group expenses by date
                val dateFormat = SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault())
                val grouped = state.expenses.groupBy { exp ->
                    dateFormat.format(Date(exp.date))
                }

                grouped.forEach { (dateStr, expensesForDate) ->
                    val daySum = expensesForDate.fold(BigDecimal.ZERO) { acc, e -> acc.add(e.amount) }

                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .border(1.dp, AtelierHairline, RoundedCornerShape(4.dp)),
                            shape = RoundedCornerShape(4.dp),
                            color = AtelierCanvas
                        ) {
                            Column {
                                // Date Header Strip
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(AtelierSurfaceChalk)
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            tint = AtelierInkMuted,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = dateStr.uppercase(),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.06.sp
                                            ),
                                            color = AtelierPrimaryInk
                                        )
                                    }
                                    Text(
                                        text = "Day Debits: ${MoneyUtils.format(daySum)}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = AtelierInkMuted
                                    )
                                }
                                HairlineDivider()

                                // Expense rows
                                expensesForDate.forEach { expense ->
                                    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(expense.date))
                                    val noteText = if (!expense.notes.isNullOrBlank()) expense.notes else "Disbursement narrative"
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onNavigateToEditExpense(expense.id) }
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Left Icon + Details
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(AtelierSurfaceChalk)
                                                    .border(1.dp, AtelierHairline, RoundedCornerShape(4.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Category,
                                                    contentDescription = null,
                                                    tint = AtelierPrimaryInk,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = expense.title,
                                                        style = MaterialTheme.typography.titleMedium.copy(
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.SemiBold
                                                        ),
                                                        color = AtelierPrimaryInk,
                                                        maxLines = 1,
                                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f, fill = false)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    AtelierPillBadge(
                                                        text = "Logged",
                                                        backgroundColor = AtelierSageSubtle,
                                                        contentColor = AtelierSage
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = noteText,
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                    color = AtelierInkMuted,
                                                    maxLines = 1,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        // Right: Amount + Timestamp
                                        Column(horizontalAlignment = Alignment.End) {
                                            Row(verticalAlignment = Alignment.Bottom) {
                                                Text(
                                                    text = "-${MoneyUtils.format(expense.amount, expense.currency)}",
                                                    fontFamily = NewsreaderFontFamily,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = AtelierCoral
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = "DR",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                    color = AtelierInkMuted
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = timeFormat,
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                                color = AtelierInkMuted
                                            )
                                        }
                                    }
                                    HairlineDivider()
                                }
                            }
                        }
                    }
                }
            }

            // 7. LEDGER SEAL RECONCILED FOOTER
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = AtelierSurfaceChalk.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AtelierHairline, RoundedCornerShape(4.dp))
                            .padding(18.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = AtelierSage,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "LEDGER SEAL RECONCILED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.08.sp
                                ),
                                color = AtelierPrimaryInk
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "End of current passbook page 24 • Balances carried forward",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    fontStyle = FontStyle.Italic
                                ),
                                color = AtelierInkMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class PrototypeEntry(
    val name: String,
    val note: String,
    val status: String,
    val statusColor: Color,
    val amount: String,
    val time: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
private fun PrototypeLedgerDateGroup(
    dateTitle: String,
    dayDebit: String,
    entries: List<PrototypeEntry>
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AtelierHairline, RoundedCornerShape(4.dp)),
        shape = RoundedCornerShape(4.dp),
        color = AtelierCanvas
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AtelierSurfaceChalk)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = AtelierInkMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dateTitle.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.06.sp
                        ),
                        color = AtelierPrimaryInk
                    )
                }
                Text(
                    text = "Day Debits: $dayDebit",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = AtelierInkMuted
                )
            }
            HairlineDivider()

            entries.forEach { entry ->
                val statusBg = when (entry.statusColor) {
                    AtelierSage -> AtelierSageSubtle
                    AtelierPeriwinkle -> AtelierPeriwinkleSubtle
                    else -> AtelierCoralSubtle
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(AtelierSurfaceChalk)
                                .border(1.dp, AtelierHairline, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = entry.icon,
                                contentDescription = null,
                                tint = AtelierPrimaryInk,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = entry.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = AtelierPrimaryInk,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                AtelierPillBadge(
                                    text = entry.status,
                                    backgroundColor = statusBg,
                                    contentColor = entry.statusColor
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = entry.note,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = AtelierInkMuted,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = entry.amount,
                            fontFamily = NewsreaderFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = AtelierCoral
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = entry.time,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = AtelierInkMuted
                        )
                    }
                }
                HairlineDivider()
            }
        }
    }
}
