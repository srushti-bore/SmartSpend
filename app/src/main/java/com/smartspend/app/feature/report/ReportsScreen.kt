package com.smartspend.app.feature.report

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartspend.app.core.money.MoneyUtils
import com.smartspend.app.core.ui.components.LoadingState
import com.smartspend.app.core.ui.theme.BrandPrimary
import com.smartspend.app.core.ui.theme.PastelBlue
import com.smartspend.app.core.ui.theme.PastelGreen
import com.smartspend.app.core.ui.theme.PastelPink
import com.smartspend.app.core.ui.theme.PastelPurple
import com.smartspend.app.core.ui.theme.PastelYellow
import com.smartspend.app.core.ui.theme.StatusDanger
import com.smartspend.app.core.ui.theme.StatusSuccess
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        LoadingState(message = "Aggregating financial reports...")
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financial Reports & Analytics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Time Range Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.selectedTimeRange == "THIS_MONTH",
                        onClick = { viewModel.setTimeRange("THIS_MONTH") },
                        label = { Text("This Month") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = BrandPrimary
                        )
                    )
                    FilterChip(
                        selected = uiState.selectedTimeRange == "LAST_MONTH",
                        onClick = { viewModel.setTimeRange("LAST_MONTH") },
                        label = { Text("Last Month") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = BrandPrimary
                        )
                    )
                    FilterChip(
                        selected = uiState.selectedTimeRange == "LAST_3_MONTHS",
                        onClick = { viewModel.setTimeRange("LAST_3_MONTHS") },
                        label = { Text("Last 3 Months") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = BrandPrimary
                        )
                    )
                }
            }

            // 2. Net Cash Flow & Savings Rate Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Net Cash Savings", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text(
                                text = "${String.format("%.1f", uiState.savingsRatePct)}% Saved",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (uiState.netSavings >= BigDecimal.ZERO) StatusSuccess else StatusDanger
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = MoneyUtils.format(uiState.netSavings, uiState.currency),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (uiState.netSavings >= BigDecimal.ZERO) StatusSuccess else StatusDanger
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = StatusSuccess.copy(alpha = 0.12f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Total Income", style = MaterialTheme.typography.labelSmall, color = StatusSuccess)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "+${MoneyUtils.format(uiState.totalIncome, uiState.currency)}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = StatusSuccess
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = StatusDanger.copy(alpha = 0.12f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.TrendingDown, contentDescription = null, tint = StatusDanger, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Total Spent", style = MaterialTheme.typography.labelSmall, color = StatusDanger)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "-${MoneyUtils.format(uiState.totalExpense, uiState.currency)}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = StatusDanger
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Category Spending Breakdown
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Category, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Category Spend Hierarchy", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        if (uiState.categoryBreakdown.isEmpty()) {
                            Text("No category expenditures recorded in this period.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            val total = if (uiState.totalExpense > BigDecimal.ZERO) uiState.totalExpense else BigDecimal.ONE
                            uiState.categoryBreakdown.forEach { cat ->
                                val pct = cat.totalAmount.multiply(BigDecimal("100")).divide(total, 1, java.math.RoundingMode.HALF_EVEN).toDouble()
                                Column(modifier = Modifier.padding(vertical = 5.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = cat.categoryName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                        Text(text = "${MoneyUtils.format(cat.totalAmount, uiState.currency)} (${String.format("%.1f", pct)}%)", style = MaterialTheme.typography.labelSmall)
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    LinearProgressIndicator(
                                        progress = (pct / 100.0).toFloat().coerceIn(0f, 1f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = BrandPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Payment Modes Distribution
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CreditCard, contentDescription = null, tint = PastelPurple, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Payment Methods Distribution", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        if (uiState.paymentDistribution.isEmpty()) {
                            Text("No payment methods recorded.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            uiState.paymentDistribution.forEach { pm ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = pm.label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                    Text(
                                        text = "${MoneyUtils.format(pm.totalAmount, uiState.currency)} (${String.format("%.1f", pm.percentage)}%)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
