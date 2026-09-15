package com.smartspend.app.feature.intelligence

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartspend.app.core.money.MoneyUtils
import com.smartspend.app.core.ui.components.AtelierPillBadge
import com.smartspend.app.core.ui.components.LoadingState
import com.smartspend.app.core.ui.components.VisualMoodMascot
import com.smartspend.app.core.ui.theme.AtelierAmber
import com.smartspend.app.core.ui.theme.AtelierAmberSubtle
import com.smartspend.app.core.ui.theme.AtelierCanvas
import com.smartspend.app.core.ui.theme.AtelierCoral
import com.smartspend.app.core.ui.theme.AtelierCoralSubtle
import com.smartspend.app.core.ui.theme.AtelierHairline
import com.smartspend.app.core.ui.theme.AtelierInkMuted
import com.smartspend.app.core.ui.theme.AtelierLavender
import com.smartspend.app.core.ui.theme.AtelierLavenderSubtle
import com.smartspend.app.core.ui.theme.AtelierPeriwinkle
import com.smartspend.app.core.ui.theme.AtelierPeriwinkleSubtle
import com.smartspend.app.core.ui.theme.AtelierPrimaryInk
import com.smartspend.app.core.ui.theme.AtelierSage
import com.smartspend.app.core.ui.theme.AtelierSageSubtle
import com.smartspend.app.core.ui.theme.AtelierSurfaceChalk
import com.smartspend.app.core.ui.theme.NewsreaderFontFamily
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

    if (uiState.isLoading) {
        LoadingState(message = "Analyzing financial intelligence...")
        return
    }

    Scaffold(
        containerColor = AtelierCanvas,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "AI Financial Intelligence",
                        fontFamily = NewsreaderFontFamily,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Medium,
                        color = AtelierPrimaryInk
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = AtelierPrimaryInk,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToAskAi) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "Ask AI",
                            tint = AtelierAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AtelierCanvas)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAskAi,
                containerColor = AtelierAmber,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ask AI",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AtelierCanvas)
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
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = AtelierSurfaceChalk,
                        border = BorderStroke(1.dp, AtelierHairline)
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
                                        fontFamily = NewsreaderFontFamily,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AtelierPrimaryInk
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = health.summaryTitle,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = AtelierInkMuted
                                    )
                                }

                                // Score Ring Badge
                                Surface(
                                    shape = CircleShape,
                                    color = when (health.grade) {
                                        "A+", "A" -> AtelierSageSubtle
                                        "B" -> AtelierPeriwinkleSubtle
                                        "C" -> AtelierAmberSubtle
                                        else -> AtelierCoralSubtle
                                    },
                                    border = BorderStroke(
                                        1.dp,
                                        when (health.grade) {
                                            "A+", "A" -> AtelierSage
                                            "B" -> AtelierPeriwinkle
                                            "C" -> AtelierAmber
                                            else -> AtelierCoral
                                        }
                                    ),
                                    modifier = Modifier.size(52.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "${health.overallScore}",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = when (health.grade) {
                                                    "A+", "A" -> AtelierSage
                                                    "B" -> AtelierPeriwinkle
                                                    "C" -> AtelierAmber
                                                    else -> AtelierCoral
                                                }
                                            )
                                            Text(
                                                text = health.grade,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.ExtraBold),
                                                color = when (health.grade) {
                                                    "A+", "A" -> AtelierSage
                                                    "B" -> AtelierPeriwinkle
                                                    "C" -> AtelierAmber
                                                    else -> AtelierCoral
                                                }
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
                                        Text(text = pillar.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = AtelierPrimaryInk)
                                        Text(text = "${pillar.score}/20 • ${pillar.status}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = AtelierInkMuted)
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    LinearProgressIndicator(
                                        progress = { pillar.score.toFloat() / 20f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = AtelierAmber,
                                        trackColor = AtelierHairline
                                    )
                                }
                            }

                            if (health.actionRecommendations.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Top Action Recommendation:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = AtelierPrimaryInk
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "👉 ${health.actionRecommendations.first()}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = AtelierInkMuted
                                )
                            }
                        }
                    }
                }
            }

            // 3. Safe-to-Spend Speedometer Card
            uiState.safeToSpend?.let { safe ->
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = AtelierSurfaceChalk,
                        border = BorderStroke(1.dp, AtelierHairline)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = AtelierSage, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Safe-to-Spend Run-Rate",
                                    fontFamily = NewsreaderFontFamily,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AtelierPrimaryInk
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AtelierSageSubtle,
                                    border = BorderStroke(1.dp, AtelierSage.copy(alpha = 0.3f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Daily Allowance", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = AtelierSage)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "₹${safe.safeDailySpend}",
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                            color = AtelierSage
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AtelierPeriwinkleSubtle,
                                    border = BorderStroke(1.dp, AtelierPeriwinkle.copy(alpha = 0.3f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Weekly Allowance", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = AtelierPeriwinkle)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "₹${safe.safeWeeklySpend}",
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                            color = AtelierPeriwinkle
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = safe.pacingAdvice,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = AtelierInkMuted
                            )
                        }
                    }
                }
            }

            // 4. Purchase Simulator Trigger Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = AtelierSurfaceChalk,
                    border = BorderStroke(1.dp, AtelierHairline)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Calculate, contentDescription = null, tint = AtelierAmber, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Purchase Simulator (\"Can I afford this?\")",
                                fontFamily = NewsreaderFontFamily,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = AtelierPrimaryInk
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = simTitleInput,
                                onValueChange = { simTitleInput = it },
                                placeholder = { Text("Item (e.g. Shoes)", style = MaterialTheme.typography.bodySmall) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AtelierAmber,
                                    unfocusedBorderColor = AtelierHairline,
                                    focusedTextColor = AtelierPrimaryInk,
                                    unfocusedTextColor = AtelierPrimaryInk
                                ),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = simAmountInput,
                                onValueChange = { simAmountInput = it },
                                placeholder = { Text("₹ Amount", style = MaterialTheme.typography.bodySmall) },
                                modifier = Modifier.weight(0.8f),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AtelierAmber,
                                    unfocusedBorderColor = AtelierHairline,
                                    focusedTextColor = AtelierPrimaryInk,
                                    unfocusedTextColor = AtelierPrimaryInk
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.simulatePurchase(simTitleInput, simAmountInput) },
                            enabled = simAmountInput.isNotBlank(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AtelierAmber,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Simulate Affordability", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        // Simulation Result Display
                        uiState.simulationResult?.let { result ->
                            Spacer(modifier = Modifier.height(14.dp))
                            val (bgColor, borderColor, textColor) = when (result.decision) {
                                SimulationDecision.SAFE_TO_BUY -> Triple(AtelierSageSubtle, AtelierSage, AtelierSage)
                                SimulationDecision.PROCEED_WITH_CAUTION -> Triple(AtelierAmberSubtle, AtelierAmber, AtelierAmber)
                                SimulationDecision.DELAY_PURCHASE -> Triple(AtelierCoralSubtle, AtelierCoral, AtelierCoral)
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = bgColor,
                                border = BorderStroke(1.dp, borderColor),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = result.verdictTitle,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = textColor
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = result.explanation,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = AtelierPrimaryInk
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Spend Forecast & 50/30/20 Ratio
            uiState.forecastReport?.let { forecast ->
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = AtelierSurfaceChalk,
                        border = BorderStroke(1.dp, AtelierHairline)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = AtelierSage, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Spend Forecast & 50/30/20 Ratio",
                                    fontFamily = NewsreaderFontFamily,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AtelierPrimaryInk
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = forecast.summaryMessage,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = AtelierInkMuted
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // 50/30/20 Breakdown Bars
                            val split = forecast.rule503020
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Needs (Target 50%): ${String.format("%.1f", split.needsPct)}%", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = AtelierPrimaryInk)
                                Text("₹${split.needsAmount}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), fontWeight = FontWeight.Bold, color = AtelierPrimaryInk)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            LinearProgressIndicator(
                                progress = { (split.needsPct / 100f).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = AtelierPeriwinkle,
                                trackColor = AtelierHairline
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Wants (Target 30%): ${String.format("%.1f", split.wantsPct)}%", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = AtelierPrimaryInk)
                                Text("₹${split.wantsAmount}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), fontWeight = FontWeight.Bold, color = AtelierPrimaryInk)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            LinearProgressIndicator(
                                progress = { (split.wantsPct / 100f).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = AtelierCoral,
                                trackColor = AtelierHairline
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Savings (Target 20%): ${String.format("%.1f", split.savingsPct)}%", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = AtelierPrimaryInk)
                                Text("₹${split.savingsAmount}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), fontWeight = FontWeight.Bold, color = AtelierPrimaryInk)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            LinearProgressIndicator(
                                progress = { (split.savingsPct / 100f).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = AtelierSage,
                                trackColor = AtelierHairline
                            )
                        }
                    }
                }
            }

            // 6. Leak Hunter Report
            uiState.leakReport?.let { leaks ->
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = AtelierSurfaceChalk,
                        border = BorderStroke(1.dp, AtelierHairline)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Search, contentDescription = null, tint = AtelierAmber, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Leak Hunter",
                                    fontFamily = NewsreaderFontFamily,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AtelierPrimaryInk
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = leaks.headline,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = AtelierInkMuted
                            )

                            if (leaks.leaksFound.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                leaks.leaksFound.forEach { leak ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = AtelierCanvas,
                                        border = BorderStroke(1.dp, AtelierHairline),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.WarningAmber,
                                                contentDescription = null,
                                                tint = when (leak.severity) {
                                                    LeakSeverity.HIGH -> AtelierCoral
                                                    LeakSeverity.MEDIUM -> AtelierAmber
                                                    LeakSeverity.LOW -> AtelierPeriwinkle
                                                },
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "${leak.title} (₹${leak.totalAmount})",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                    color = AtelierPrimaryInk
                                                )
                                                Text(
                                                    text = leak.explanation,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                    color = AtelierInkMuted
                                                )
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
