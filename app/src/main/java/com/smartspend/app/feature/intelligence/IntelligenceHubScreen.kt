package com.smartspend.app.feature.intelligence

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartspend.app.core.money.MoneyUtils
import com.smartspend.app.core.ui.components.LoadingState
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
import com.smartspend.app.core.ui.theme.StatusWarning
import com.smartspend.app.domain.intelligence.LeakSeverity
import com.smartspend.app.domain.intelligence.SafeSpendTier
import com.smartspend.app.domain.intelligence.SimulationDecision

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntelligenceHubScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAskAi: () -> Unit,
    viewModel: IntelligenceHubViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var simTitleInput by remember { mutableStateOf("") }
    var simAmountInput by remember { mutableStateOf("") }
    var showSimulatorDialog by remember { mutableStateOf(false) }

    if (uiState.isLoading) {
        LoadingState(message = "Analyzing financial intelligence...")
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Financial Intelligence", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToAskAi) {
                        Icon(imageVector = Icons.Default.Chat, contentDescription = "Ask AI", tint = BrandPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAskAi,
                containerColor = BrandPrimary,
                contentColor = Color.Black,
                shape = CircleShape
            ) {
                Row(modifier = Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ask AI", fontWeight = FontWeight.Bold)
                }
            }
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
            // 1. Brainy Visual Mascot Ambient Status
            uiState.safeToSpend?.let { safe ->
                item {
                    VisualMoodMascot(
                        tier = safe.statusTier,
                        percentageUsed = safe.percentageUsed,
                        safeDaily = safe.safeDailySpend.toPlainString(),
                        onMascotClick = onNavigateToAskAi
                    )
                }
            }

            // 2. Financial Health Score Bento Card (0 - 100)
            uiState.healthReport?.let { health ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Financial Health Score",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = health.summaryTitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Score Ring Badge
                                Surface(
                                    shape = CircleShape,
                                    color = when (health.grade) {
                                        "A+", "A" -> StatusSuccess
                                        "B" -> Color(0xFF4CD7F6)
                                        "C" -> StatusWarning
                                        else -> StatusDanger
                                    },
                                    modifier = Modifier.size(54.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "${health.overallScore}",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = Color.Black
                                            )
                                            Text(
                                                text = health.grade,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                                color = Color.Black
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 5 Pillars
                            health.pillars.forEach { pillar ->
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = pillar.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                        Text(text = "${pillar.score}/20 • ${pillar.status}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    LinearProgressIndicator(
                                        progress = pillar.score.toFloat() / 20f,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = BrandPrimary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            }

                            if (health.actionRecommendations.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(text = "Top Action Recommendation:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = "👉 ${health.actionRecommendations.first()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // 3. Safe-to-Spend Speedometer Card
            uiState.safeToSpend?.let { safe ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Safe-to-Spend Run-Rate",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = BrandPrimary.copy(alpha = 0.15f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Daily Limit", style = MaterialTheme.typography.labelSmall, color = BrandPrimary)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "₹${safe.safeDailySpend}",
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                            color = BrandPrimary
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = PastelBlue.copy(alpha = 0.35f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Weekly Limit", style = MaterialTheme.typography.labelSmall)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "₹${safe.safeWeeklySpend}",
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = safe.pacingAdvice,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 4. Purchase Simulator Trigger Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Calculate, contentDescription = null, tint = PastelYellow, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Purchase Simulator (\"Can I afford this?\")", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = simTitleInput,
                                onValueChange = { simTitleInput = it },
                                placeholder = { Text("Item (e.g. Shoes)", style = MaterialTheme.typography.bodySmall) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = simAmountInput,
                                onValueChange = { simAmountInput = it },
                                placeholder = { Text("₹ Amount", style = MaterialTheme.typography.bodySmall) },
                                modifier = Modifier.weight(0.8f),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { viewModel.simulatePurchase(simTitleInput, simAmountInput) },
                            enabled = simAmountInput.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Simulate Affordability", color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        // Simulation Result Display
                        uiState.simulationResult?.let { result ->
                            Spacer(modifier = Modifier.height(14.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        when (result.decision) {
                                            SimulationDecision.SAFE_TO_BUY -> StatusSuccess.copy(alpha = 0.12f)
                                            SimulationDecision.PROCEED_WITH_CAUTION -> StatusWarning.copy(alpha = 0.12f)
                                            SimulationDecision.DELAY_PURCHASE -> StatusDanger.copy(alpha = 0.12f)
                                        }
                                    )
                                    .border(
                                        1.dp,
                                        when (result.decision) {
                                            SimulationDecision.SAFE_TO_BUY -> StatusSuccess.copy(alpha = 0.5f)
                                            SimulationDecision.PROCEED_WITH_CAUTION -> StatusWarning.copy(alpha = 0.5f)
                                            SimulationDecision.DELAY_PURCHASE -> StatusDanger.copy(alpha = 0.5f)
                                        },
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = result.verdictTitle,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = when (result.decision) {
                                            SimulationDecision.SAFE_TO_BUY -> StatusSuccess
                                            SimulationDecision.PROCEED_WITH_CAUTION -> StatusWarning
                                            SimulationDecision.DELAY_PURCHASE -> StatusDanger
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = result.explanation, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }

            // 5. Spend Forecast & 50/30/20 Ratio
            uiState.forecastReport?.let { forecast ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = PastelGreen, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Spend Forecast & 50/30/20 Ratio", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = forecast.summaryMessage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            Spacer(modifier = Modifier.height(14.dp))

                            // 50/30/20 Breakdown Bars
                            val split = forecast.rule503020
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Needs (Target 50%): ${String.format("%.1f", split.needsPct)}%", style = MaterialTheme.typography.labelSmall)
                                Text("₹${split.needsAmount}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                            LinearProgressIndicator(
                                progress = (split.needsPct / 100f).toFloat().coerceIn(0f, 1f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = PastelBlue
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Wants (Target 30%): ${String.format("%.1f", split.wantsPct)}%", style = MaterialTheme.typography.labelSmall)
                                Text("₹${split.wantsAmount}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                            LinearProgressIndicator(
                                progress = (split.wantsPct / 100f).toFloat().coerceIn(0f, 1f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = PastelPink
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Savings (Target 20%): ${String.format("%.1f", split.savingsPct)}%", style = MaterialTheme.typography.labelSmall)
                                Text("₹${split.savingsAmount}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                            LinearProgressIndicator(
                                progress = (split.savingsPct / 100f).toFloat().coerceIn(0f, 1f),
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

            // 6. Leak Hunter Report
            uiState.leakReport?.let { leaks ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Search, contentDescription = null, tint = StatusWarning, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Leak Hunter", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = leaks.headline, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            if (leaks.leaksFound.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                leaks.leaksFound.forEach { leak ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.WarningAmber,
                                                contentDescription = null,
                                                tint = when (leak.severity) {
                                                    LeakSeverity.HIGH -> StatusDanger
                                                    LeakSeverity.MEDIUM -> StatusWarning
                                                    LeakSeverity.LOW -> PastelBlue
                                                },
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(text = "${leak.title} (₹${leak.totalAmount})", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                                Text(text = leak.explanation, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}
