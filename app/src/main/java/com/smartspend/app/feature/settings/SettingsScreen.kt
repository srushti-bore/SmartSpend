package com.smartspend.app.feature.settings

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartspend.app.core.money.MoneyUtils
import com.smartspend.app.core.ui.components.AtelierPillBadge
import com.smartspend.app.core.ui.components.DoubleHairlineRule
import com.smartspend.app.core.ui.components.HairlineDivider
import com.smartspend.app.core.ui.components.PatternLockView
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
import com.smartspend.app.core.ui.theme.AtelierLavender
import com.smartspend.app.core.ui.theme.AtelierLavenderSubtle
import com.smartspend.app.core.ui.theme.AtelierPrimaryInk
import com.smartspend.app.core.ui.theme.AtelierSage
import com.smartspend.app.core.ui.theme.AtelierSageSubtle
import com.smartspend.app.core.ui.theme.AtelierSurfaceChalk
import com.smartspend.app.core.ui.theme.AtelierSurfaceChalkHigh
import com.smartspend.app.core.ui.theme.IBMPlexSansFontFamily
import com.smartspend.app.core.ui.theme.NewsreaderFontFamily
import com.smartspend.app.domain.model.AuthType
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SettingsSubScreen(val title: String) {
    DISPLAY_REGION("Display & Region"),
    BUDGET_ALERTS("Budget & Alerts"),
    ACCOUNT_SECURITY("Account & Security"),
    DATA_BACKUP("Data & Backup"),
    NOTIFICATIONS("Notifications"),
    ANALYTICS("Analytics & Spending Velocity"),
    TRANSACTION_HISTORY("Transaction History"),
    SYSTEM_ABOUT("System & About")
}

data class SettingRowItem(
    val subScreen: SettingsSubScreen,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconTint: Color,
    val categoryGroup: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    onLogout: () -> Unit = {},
    onAccountReset: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    var activeSubScreen by remember { mutableStateOf<SettingsSubScreen?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Intercept back button when inside a sub-screen
    BackHandler(enabled = activeSubScreen != null) {
        activeSubScreen = null
    }

    val restoreFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.restoreFromBackupFile(context, uri)
        }
    }

    // 1. Password / PIN / Pattern Reset Dialog
    if (state.isResetPinDialogOpen) {
        AlertDialog(
            onDismissRequest = viewModel::closeResetPinDialog,
            containerColor = AtelierCanvas,
            title = {
                Text(
                    text = "Security & Passcode Lock",
                    fontFamily = NewsreaderFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = AtelierPrimaryInk
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Choose your preferred lock method to protect ledger records.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AtelierInkMuted
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AuthType.values().forEach { type ->
                            FilterChip(
                                selected = state.selectedAuthType == type,
                                onClick = { viewModel.onAuthTypeSelected(type) },
                                label = { Text(type.name, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AtelierPrimaryInk,
                                    selectedLabelColor = AtelierCanvas
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (state.selectedAuthType == AuthType.PATTERN) {
                        Surface(
                            color = if (state.patternStep == 3) AtelierSageSubtle else AtelierSurfaceChalk,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = when (state.patternStep) {
                                        1 -> "Step 1: Draw new pattern"
                                        2 -> "Step 2: Draw again to confirm"
                                        else -> "✓ Pattern Confirmed"
                                    },
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (state.patternStep == 3) AtelierSage else AtelierPrimaryInk
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = state.patternHint,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = AtelierInkMuted
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                PatternLockView(
                                    size = 230.dp,
                                    minDots = 4,
                                    isError = state.isPatternError,
                                    enabled = state.patternStep < 3,
                                    onPatternStarted = viewModel::onPatternStarted,
                                    onPatternCompleted = viewModel::onPatternCompleted
                                )

                                if (state.patternStep > 1) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    TextButton(onClick = viewModel::resetPatternInput) {
                                        Text("Redraw Pattern", color = AtelierAmber, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    } else {
                        val keyboardType = if (state.selectedAuthType == AuthType.PIN) KeyboardType.NumberPassword else KeyboardType.Password
                        OutlinedTextField(
                            value = state.newPinInput,
                            onValueChange = viewModel::onNewPinChange,
                            label = { Text("New ${state.selectedAuthType.name}") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(4.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = state.confirmPinInput,
                            onValueChange = viewModel::onConfirmPinChange,
                            label = { Text("Confirm ${state.selectedAuthType.name}") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(4.dp)
                        )
                    }

                    if (state.pinErrorMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = state.pinErrorMessage ?: "",
                            color = AtelierCoral,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = viewModel::saveNewCredential,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AtelierPrimaryInk,
                        contentColor = AtelierCanvas
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Save Passcode")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::closeResetPinDialog) {
                    Text("Cancel", color = AtelierInkMuted)
                }
            }
        )
    }

    // 2. Delete Account Confirmation Dialog
    if (state.isDeleteAccountDialogOpen) {
        AlertDialog(
            onDismissRequest = viewModel::closeDeleteAccountDialog,
            containerColor = AtelierCanvas,
            title = {
                Text(
                    text = "Delete Account & Wipe Database?",
                    fontFamily = NewsreaderFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AtelierCoral
                )
            },
            text = {
                Text(
                    text = "This will permanently erase all ledger vouchers, budgets, categories, and encryption keys from your device. This action is irreversible.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AtelierPrimaryInk
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.wipeAndResetAccount {
                            onAccountReset()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AtelierCoral,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Permanently Delete & Wipe")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::closeDeleteAccountDialog) {
                    Text("Cancel", color = AtelierInkMuted)
                }
            }
        )
    }

    // Main screen vs Sub-Screen transition
    AnimatedContent(
        targetState = activeSubScreen,
        transitionSpec = {
            if (targetState != null) {
                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                    slideOutHorizontally { width -> -width } + fadeOut()
                )
            } else {
                (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                    slideOutHorizontally { width -> width } + fadeOut()
                )
            }
        },
        label = "SettingsDrillDown"
    ) { subScreen ->
        if (subScreen == null) {
            // ==========================================
            // INSTAGRAM-STYLE "SETTINGS & ACTIVITY" LIST
            // ==========================================
            InstagramSettingsListView(
                state = state,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onSelectSubScreen = { activeSubScreen = it },
                onLogout = { viewModel.logout { onLogout() } }
            )
        } else {
            // ==========================================
            // DEDICATED SUB-SCREEN VIEW WITH TOP APP BAR
            // ==========================================
            SubScreenContainer(
                title = subScreen.title,
                onBack = { activeSubScreen = null }
            ) {
                when (subScreen) {
                    SettingsSubScreen.DISPLAY_REGION -> DisplayRegionContent(state, viewModel)
                    SettingsSubScreen.BUDGET_ALERTS -> BudgetAlertsContent(state, viewModel)
                    SettingsSubScreen.ACCOUNT_SECURITY -> AccountSecurityContent(state, viewModel, onLogout)
                    SettingsSubScreen.DATA_BACKUP -> DataBackupContent(state, viewModel, context, onRestoreClick = { restoreFilePicker.launch(arrayOf("*/*")) })
                    SettingsSubScreen.NOTIFICATIONS -> NotificationsContent(state, viewModel)
                    SettingsSubScreen.ANALYTICS -> AnalyticsContent(state, viewModel)
                    SettingsSubScreen.TRANSACTION_HISTORY -> TransactionHistoryContent(state, viewModel, context)
                    SettingsSubScreen.SYSTEM_ABOUT -> SystemAboutContent()
                }
            }
        }
    }
}

// ==========================================
// 1. INSTAGRAM-STYLE SETTINGS LIST COMPOSABLE
// ==========================================

@Composable
private fun InstagramSettingsListView(
    state: SettingsUiState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSelectSubScreen: (SettingsSubScreen) -> Unit,
    onLogout: () -> Unit
) {
    val allSettings = remember {
        listOf(
            SettingRowItem(
                subScreen = SettingsSubScreen.DISPLAY_REGION,
                title = "Display & Region",
                subtitle = "Theme, currency, date & number formats",
                icon = Icons.Default.Palette,
                iconTint = Color(0xFFE5855E),
                categoryGroup = "PREFERENCES & INTERFACE"
            ),
            SettingRowItem(
                subScreen = SettingsSubScreen.NOTIFICATIONS,
                title = "Notifications",
                subtitle = "Daily 9:00 PM reminder, spend milestone alerts",
                icon = Icons.Default.Notifications,
                iconTint = Color(0xFFD97706),
                categoryGroup = "PREFERENCES & INTERFACE"
            ),
            SettingRowItem(
                subScreen = SettingsSubScreen.BUDGET_ALERTS,
                title = "Budget & Alerts",
                subtitle = "80% limit warning, deficit alerts, daily pacing",
                icon = Icons.Default.PieChart,
                iconTint = Color(0xFFDC2626),
                categoryGroup = "FINANCIAL MANAGEMENT"
            ),
            SettingRowItem(
                subScreen = SettingsSubScreen.ANALYTICS,
                title = "Analytics & Spending Velocity",
                subtitle = "Daily, weekly, monthly bar & category donut charts",
                icon = Icons.Default.Analytics,
                iconTint = Color(0xFF6366F1),
                categoryGroup = "FINANCIAL MANAGEMENT"
            ),
            SettingRowItem(
                subScreen = SettingsSubScreen.TRANSACTION_HISTORY,
                title = "Transaction History",
                subtitle = "Month-wise voucher ledger, Export CSV & PDF",
                icon = Icons.Default.History,
                iconTint = Color(0xFF2563EB),
                categoryGroup = "RECORDS & DATA"
            ),
            SettingRowItem(
                subScreen = SettingsSubScreen.DATA_BACKUP,
                title = "Data & Backup",
                subtitle = "Hardware AES-256 encrypted .smartspend archives",
                icon = Icons.Default.Storage,
                iconTint = Color(0xFF059669),
                categoryGroup = "RECORDS & DATA"
            ),
            SettingRowItem(
                subScreen = SettingsSubScreen.ACCOUNT_SECURITY,
                title = "Account & Security",
                subtitle = "PIN, Pattern, or Password lock, data wipe",
                icon = Icons.Default.Security,
                iconTint = Color(0xFF7C3AED),
                categoryGroup = "SECURITY & SYSTEM"
            ),
            SettingRowItem(
                subScreen = SettingsSubScreen.SYSTEM_ABOUT,
                title = "System & About",
                subtitle = "v1.0.0, SQLCipher 4.6.0, 100% on-device guarantee",
                icon = Icons.Default.Info,
                iconTint = Color(0xFF475569),
                categoryGroup = "SECURITY & SYSTEM"
            )
        )
    }

    val filteredList = remember(searchQuery) {
        if (searchQuery.isBlank()) allSettings
        else allSettings.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.subtitle.contains(searchQuery, ignoreCase = true) ||
                    it.categoryGroup.contains(searchQuery, ignoreCase = true)
        }
    }

    val profile = state.activeProfile
    val profileName = profile?.name?.ifBlank { "Srushti" } ?: "Srushti"
    val monogram = profileName.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("").ifBlank { "S" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AtelierCanvas)
            .padding(horizontal = 20.dp)
    ) {
        // Top Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(AtelierSurfaceChalk)
                                .border(1.5.dp, AtelierAmber, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = monogram,
                                fontFamily = NewsreaderFontFamily,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AtelierPrimaryInk
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Settings and activity",
                                fontFamily = NewsreaderFontFamily,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Medium,
                                color = AtelierPrimaryInk
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = "PRIMARY FOLIO NO. 412 • $profileName",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = AtelierInkMuted
                            )
                        }
                    }

                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock Session",
                            tint = AtelierPrimaryInk,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HairlineDivider()
            }
        }

        // Search Bar
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .border(1.dp, AtelierHairline, RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                color = AtelierSurfaceChalk.copy(alpha = 0.6f)
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
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        textStyle = TextStyle(
                            fontFamily = IBMPlexSansFontFamily,
                            fontSize = 13.sp,
                            color = AtelierPrimaryInk
                        ),
                        cursorBrush = SolidColor(AtelierAmber),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search settings, preferences, data...",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 13.sp,
                                        color = AtelierInkMuted.copy(alpha = 0.6f)
                                    )
                                )
                            }
                            innerTextField()
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchQueryChange("") },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = AtelierInkMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Grouped Settings List
        if (searchQuery.isNotBlank()) {
            // Search Results Mode
            if (filteredList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No settings found matching \"$searchQuery\"",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = AtelierInkMuted
                        )
                    }
                }
            } else {
                items(filteredList) { item ->
                    InstagramSettingRow(item = item, onClick = { onSelectSubScreen(item.subScreen) })
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        } else {
            // Categorized Instagram Groupings
            val groups = listOf(
                "PREFERENCES & INTERFACE",
                "FINANCIAL MANAGEMENT",
                "RECORDS & DATA",
                "SECURITY & SYSTEM"
            )

            groups.forEach { groupTitle ->
                val groupItems = allSettings.filter { it.categoryGroup == groupTitle }
                if (groupItems.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(top = 14.dp, bottom = 6.dp)) {
                            Text(
                                text = groupTitle,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.08.sp
                                ),
                                color = AtelierInkMuted
                            )
                        }
                    }

                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, AtelierHairline, RoundedCornerShape(8.dp)),
                            shape = RoundedCornerShape(8.dp),
                            color = AtelierCanvas
                        ) {
                            Column {
                                groupItems.forEachIndexed { index, item ->
                                    InstagramSettingRow(
                                        item = item,
                                        onClick = { onSelectSubScreen(item.subScreen) }
                                    )
                                    if (index < groupItems.size - 1) {
                                        HairlineDivider(modifier = Modifier.padding(start = 54.dp))
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun InstagramSettingRow(
    item: SettingRowItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(item.iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = item.iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = AtelierPrimaryInk
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = AtelierInkMuted,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = AtelierInkMuted.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp)
        )
    }
}

// ==========================================
// 2. SUB-SCREEN CONTAINER
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubScreenContainer(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        containerColor = AtelierCanvas,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        fontFamily = NewsreaderFontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = AtelierPrimaryInk
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back to Settings",
                            tint = AtelierPrimaryInk,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AtelierCanvas)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AtelierCanvas)
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            HairlineDivider()
            Spacer(modifier = Modifier.height(16.dp))
            content()
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ==========================================
// SUB-SCREEN CONTENTS (ALL 8 PRESERVED)
// ==========================================

@Composable
private fun DisplayRegionContent(state: SettingsUiState, viewModel: SettingsViewModel) {
    SectionCard(title = "sRGB Pastel Themes (Derived Harmonics)") {
        Text(
            text = "Choose your preferred sRGB pastel color atmosphere. All themes use soft, curated tones without heavy green tinting.",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
            color = AtelierInkMuted
        )
        Spacer(modifier = Modifier.height(14.dp))

        val themes = listOf(
            PastelThemeInfo(
                key = "LIGHT",
                name = "Classic Light",
                subtitle = "Warm alabaster paper • Slate & Sand",
                canvasColor = Color(0xFFFBFBF9),
                inkColor = Color(0xFF1E232A),
                accent1 = Color(0xFF6C8EBF),
                accent2 = Color(0xFFE5A962),
                accent3 = Color(0xFFD97D72)
            ),
            PastelThemeInfo(
                key = "DARK",
                name = "Obsidian Dark",
                subtitle = "Deep charcoal slate • Periwinkle & Amber",
                canvasColor = Color(0xFF121519),
                inkColor = Color(0xFFF0F2F5),
                accent1 = Color(0xFF8EA8D6),
                accent2 = Color(0xFFF0B86E),
                accent3 = Color(0xFFE58B82)
            ),
            PastelThemeInfo(
                key = "PASTEL_LAVENDER",
                name = "Pastel Lavender",
                subtitle = "Airy misty lilac • Iris & Mauve",
                canvasColor = Color(0xFFF8F6FB),
                inkColor = Color(0xFF201B2E),
                accent1 = Color(0xFF8B7BB5),
                accent2 = Color(0xFFD67C96),
                accent3 = Color(0xFFE8BA7A)
            ),
            PastelThemeInfo(
                key = "PASTEL_SAND",
                name = "Warm Sand & Dune",
                subtitle = "Desert linen • Terracotta & Caramel",
                canvasColor = Color(0xFFFAF7F2),
                inkColor = Color(0xFF2C221A),
                accent1 = Color(0xFFD4836A),
                accent2 = Color(0xFFDFA35C),
                accent3 = Color(0xFF6E8FA8)
            ),
            PastelThemeInfo(
                key = "PASTEL_OCEAN",
                name = "Nordic Mist",
                subtitle = "Fresh pale sky • Cerulean & Apricot",
                canvasColor = Color(0xFFF4F8FA),
                inkColor = Color(0xFF17232E),
                accent1 = Color(0xFF5E93B5),
                accent2 = Color(0xFFD88373),
                accent3 = Color(0xFF6FA398)
            ),
            PastelThemeInfo(
                key = "SYSTEM",
                name = "System Auto",
                subtitle = "Follows your Android device settings",
                canvasColor = Color(0xFFEFEFEA),
                inkColor = Color(0xFF2B323D),
                accent1 = Color(0xFF6C8EBF),
                accent2 = Color(0xFFE5A962),
                accent3 = Color(0xFF6B9A89)
            )
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            themes.forEach { theme ->
                val isSelected = state.themeMode.equals(theme.key, ignoreCase = true)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSelected) AtelierSurfaceChalkHigh else AtelierSurfaceChalk.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) AtelierPrimaryInk else AtelierHairline,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { viewModel.setThemeMode(theme.key) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Theme Swatch Pill Preview
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(theme.canvasColor)
                                    .border(1.dp, Color(0xFFD0D0D0), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(theme.accent1))
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(theme.accent2))
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(theme.accent3))
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = theme.name,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        fontSize = 13.5.sp
                                    ),
                                    color = AtelierPrimaryInk
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = theme.subtitle,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                                    color = AtelierInkMuted
                                )
                            }
                        }

                        if (isSelected) {
                            Surface(
                                shape = RoundedCornerShape(9999.dp),
                                color = AtelierPrimaryInk
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.05.sp
                                    ),
                                    color = AtelierCanvas,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    SectionCard(title = "Preferred Currency & Format") {
        Text(
            text = "Select your accounting currency. Number formats, symbols, and summaries across the entire app will synchronize instantly.",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
            color = AtelierInkMuted
        )
        Spacer(modifier = Modifier.height(14.dp))

        val currencyOptions = listOf(
            CurrencyOptionItem("INR", "₹", "Indian Rupee (INR)", "₹1,23,456.78 (Lakhs format)"),
            CurrencyOptionItem("USD", "$", "US Dollar (USD)", "$123,456.78 (Standard decimal)"),
            CurrencyOptionItem("EUR", "€", "Euro (EUR)", "€123,456.78 (European standard)"),
            CurrencyOptionItem("GBP", "£", "British Pound (GBP)", "£123,456.78 (UK standard)"),
            CurrencyOptionItem("JPY", "¥", "Japanese Yen (JPY)", "¥123,456 (Zero decimal)")
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            currencyOptions.forEach { item ->
                val isSelected = state.preferredCurrency.equals(item.code, ignoreCase = true)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSelected) AtelierSurfaceChalkHigh else AtelierSurfaceChalk.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) AtelierPrimaryInk else AtelierHairline,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { viewModel.setPreferredCurrency(item.code) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) AtelierPrimaryInk else AtelierSurfaceChalk)
                                    .border(1.dp, AtelierHairline, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item.symbol,
                                    fontFamily = NewsreaderFontFamily,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) AtelierCanvas else AtelierPrimaryInk
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        fontSize = 13.5.sp
                                    ),
                                    color = AtelierPrimaryInk
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.sample,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                                    color = AtelierInkMuted
                                )
                            }
                        }

                        if (isSelected) {
                            Surface(
                                shape = RoundedCornerShape(9999.dp),
                                color = AtelierPrimaryInk
                            ) {
                                Text(
                                    text = "SELECTED",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.05.sp
                                    ),
                                    color = AtelierCanvas,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class PastelThemeInfo(
    val key: String,
    val name: String,
    val subtitle: String,
    val canvasColor: Color,
    val inkColor: Color,
    val accent1: Color,
    val accent2: Color,
    val accent3: Color
)

private data class CurrencyOptionItem(
    val code: String,
    val symbol: String,
    val title: String,
    val sample: String
)

@Composable
private fun BudgetAlertsContent(state: SettingsUiState, viewModel: SettingsViewModel) {
    SectionCard(title = "Alert Triggers & Safety Pacing") {
        SettingSwitchRow(
            title = "80% Budget Warning Alert",
            subtitle = "Alert when spend velocity exceeds 80% of monthly allocation",
            isChecked = state.alertThreshold80,
            onToggle = viewModel::toggleAlertThreshold80
        )
        Spacer(modifier = Modifier.height(10.dp))
        HairlineDivider()
        Spacer(modifier = Modifier.height(10.dp))

        SettingSwitchRow(
            title = "100% Deficit & Overrun Alert",
            subtitle = "Immediate warning when spending enters negative balance",
            isChecked = state.alertThreshold100,
            onToggle = viewModel::toggleAlertThreshold100
        )
        Spacer(modifier = Modifier.height(10.dp))
        HairlineDivider()
        Spacer(modifier = Modifier.height(10.dp))

        SettingSwitchRow(
            title = "Daily Pacing & Safe-to-Spend",
            subtitle = "Show dynamic daily run-rate recommendations",
            isChecked = state.dailyPacingAlerts,
            onToggle = viewModel::toggleDailyPacingAlerts
        )
        Spacer(modifier = Modifier.height(10.dp))
        HairlineDivider()
        Spacer(modifier = Modifier.height(10.dp))

        SettingSwitchRow(
            title = "Recurring Subscription Alerts",
            subtitle = "Notify 24 hours prior to scheduled billing cycles",
            isChecked = state.recurringDueAlerts,
            onToggle = viewModel::toggleRecurringDueAlerts
        )
    }
}

@Composable
private fun AccountSecurityContent(
    state: SettingsUiState,
    viewModel: SettingsViewModel,
    onLogout: () -> Unit
) {
    SectionCard(title = "Passbook Authentication & Session") {
        SettingActionRow(
            title = "Security & Passcode Lock",
            subtitle = "Configure PIN, Pattern, or Password authentication",
            icon = Icons.Default.Key,
            onClick = viewModel::openResetPinDialog
        )
        Spacer(modifier = Modifier.height(10.dp))
        HairlineDivider()
        Spacer(modifier = Modifier.height(10.dp))

        SettingActionRow(
            title = "Lock Passbook Session",
            subtitle = "Immediately lock session and require passcode",
            icon = Icons.Default.Lock,
            onClick = onLogout
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    SectionCard(title = "Danger Zone") {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.openDeleteAccountDialog() }
                .border(1.dp, AtelierCoral.copy(alpha = 0.4f), RoundedCornerShape(4.dp)),
            shape = RoundedCornerShape(4.dp),
            color = AtelierCoralSubtle.copy(alpha = 0.3f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = null, tint = AtelierCoral, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Wipe Database & Reset Account",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold),
                        color = AtelierCoral
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Permanently erase all vouchers and reset to initial onboarding",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = AtelierInkMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun DataBackupContent(
    state: SettingsUiState,
    viewModel: SettingsViewModel,
    context: android.content.Context,
    onRestoreClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = AtelierSageSubtle.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Security, contentDescription = null, tint = AtelierSage, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Backups are cryptographically encrypted with AES-256 via Android Keystore. Your financial records are 100% private.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                color = AtelierPrimaryInk
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    state.restoreSummary?.let { summary ->
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = AtelierSageSubtle,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "✓ Backup Restored Successfully",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = AtelierSage
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• ${summary.expensesCount} Expenses  • ${summary.incomesCount} Incomes\n• ${summary.categoriesCount} Categories  • ${summary.savingsGoalsCount} Goals",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = AtelierPrimaryInk
                )
            }
        }
    }

    SectionCard(title = "Hardware-Backed Encrypted Archive") {
        Button(
            onClick = { viewModel.exportEncryptedBackup(context) },
            enabled = !state.isExportingBackup,
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AtelierPrimaryInk,
                contentColor = AtelierCanvas
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isExportingBackup) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = AtelierCanvas, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Creating Encrypted Archive...")
            } else {
                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export Encrypted Backup (.smartspend)")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onRestoreClick,
            enabled = !state.isRestoringBackup,
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isRestoringBackup) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Decrypting & Restoring Ledger...")
            } else {
                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Restore from .smartspend File")
            }
        }
    }
}

@Composable
private fun NotificationsContent(state: SettingsUiState, viewModel: SettingsViewModel) {
    SectionCard(title = "Reminder Preferences") {
        SettingSwitchRow(
            title = "Daily Ledger Reminder (9:00 PM)",
            subtitle = "Prompts you to record any unlogged daily vouchers",
            isChecked = state.dailyReminderEnabled,
            onToggle = viewModel::toggleDailyReminder
        )
        Spacer(modifier = Modifier.height(10.dp))
        HairlineDivider()
        Spacer(modifier = Modifier.height(10.dp))

        SettingSwitchRow(
            title = "Spend Milestone Celebrations",
            subtitle = "Celebrate disciplined budgeting milestones (≤50%)",
            isChecked = state.milestoneAlertsEnabled,
            onToggle = viewModel::toggleMilestoneAlerts
        )
    }
}

@Composable
private fun AnalyticsContent(state: SettingsUiState, viewModel: SettingsViewModel) {
    SectionCard(title = "Timeframe Selection") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("DAILY" to "Daily", "WEEKLY" to "Weekly", "MONTHLY" to "Monthly").forEach { (tf, label) ->
                val isSelected = state.analyticsTimeframe == tf
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setAnalyticsTimeframe(tf) },
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AtelierPrimaryInk,
                        selectedLabelColor = AtelierCanvas,
                        containerColor = AtelierSurfaceChalk,
                        labelColor = AtelierPrimaryInk
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                SectionLabel(text = "Total Period Outflow")
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = MoneyUtils.format(state.analyticsTotalSpend, state.preferredCurrency),
                    fontFamily = NewsreaderFontFamily,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = AtelierCoral
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                SectionLabel(text = "Peak Interval")
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = MoneyUtils.format(state.analyticsPeakSpend, state.preferredCurrency),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                    color = AtelierPrimaryInk
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionLabel(text = "Spending Trend (${state.analyticsTimeframe})")
        Spacer(modifier = Modifier.height(8.dp))

        if (state.analyticsBarItems.isEmpty() || state.analyticsTotalSpend == BigDecimal.ZERO) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = AtelierSurfaceChalk,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "No debit transactions recorded for this period",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = AtelierInkMuted
                    )
                }
            }
        } else {
            SpendingBarChart(
                items = state.analyticsBarItems,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        HairlineDivider()
        Spacer(modifier = Modifier.height(16.dp))

        SectionLabel(text = "Category Distribution")
        Spacer(modifier = Modifier.height(8.dp))

        if (state.analyticsDonutSegments.isEmpty() || state.analyticsTotalSpend == BigDecimal.ZERO) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = AtelierSurfaceChalk,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "No category allocations recorded",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = AtelierInkMuted
                    )
                }
            }
        } else {
            CategoryDonutChart(
                segments = state.analyticsDonutSegments,
                totalAmount = state.analyticsTotalSpend,
                currency = state.preferredCurrency,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TransactionHistoryContent(
    state: SettingsUiState,
    viewModel: SettingsViewModel,
    context: android.content.Context
) {
    SectionCard(title = "Monthly Statement & Ledger") {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = AtelierSurfaceChalk,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = viewModel::previousMonth,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month", tint = AtelierPrimaryInk)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.historyMonthTitle.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.08.sp
                        ),
                        color = AtelierPrimaryInk
                    )
                    Text(
                        text = "Debits: ${MoneyUtils.format(state.historyTotalDebit, state.preferredCurrency)}  •  Credits: ${MoneyUtils.format(state.historyTotalCredit, state.preferredCurrency)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = AtelierInkMuted
                    )
                }

                IconButton(
                    onClick = viewModel::nextMonth,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Month", tint = AtelierPrimaryInk)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.exportMonthCsv(context) },
                enabled = !state.isHistoryExporting,
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AtelierPrimaryInk,
                    contentColor = AtelierCanvas
                ),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export CSV", fontSize = 12.sp)
            }

            Button(
                onClick = { viewModel.exportMonthPdf(context) },
                enabled = !state.isHistoryExporting,
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AtelierCoral,
                    contentColor = Color.White
                ),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export PDF", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HairlineDivider()
        Spacer(modifier = Modifier.height(12.dp))

        if (state.historyExpenses.isEmpty() && state.historyIncomes.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = AtelierSurfaceChalk.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No transaction vouchers for ${state.historyMonthTitle}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = AtelierInkMuted
                    )
                }
            }
        } else {
            val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.historyExpenses.take(30).forEach { exp ->
                    val catName = state.categoriesMap[exp.categoryId]?.name ?: "General"
                    val pmLabel = state.paymentMethodsMap[exp.paymentMethodId]?.label ?: "Cash"
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = AtelierCanvas,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AtelierHairline, RoundedCornerShape(4.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = exp.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
                                        color = AtelierPrimaryInk
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    AtelierPillBadge(text = catName, backgroundColor = AtelierSurfaceChalk, contentColor = AtelierInkMuted)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${dateFormat.format(Date(exp.date))} • $pmLabel",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                    color = AtelierInkMuted
                                )
                            }

                            Text(
                                text = "-${MoneyUtils.format(exp.amount, state.preferredCurrency)}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                                color = AtelierCoral
                            )
                        }
                    }
                }

                state.historyIncomes.take(15).forEach { inc ->
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = AtelierCanvas,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AtelierHairline, RoundedCornerShape(4.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = inc.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
                                        color = AtelierPrimaryInk
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    AtelierPillBadge(text = inc.source.displayName, backgroundColor = AtelierSageSubtle, contentColor = AtelierSage)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = dateFormat.format(Date(inc.date)),
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                    color = AtelierInkMuted
                                )
                            }

                            Text(
                                text = "+${MoneyUtils.format(inc.amount, state.preferredCurrency)}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                                color = AtelierSage
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SystemAboutContent() {
    SectionCard(title = "System Specifications & Guarantee") {
        InfoPropertyRow(label = "Application Version", value = "1.0.0 (Build 412)")
        Spacer(modifier = Modifier.height(8.dp))
        InfoPropertyRow(label = "Database Engine", value = "SQLCipher 4.6.0 (AES-256 Encrypted)")
        Spacer(modifier = Modifier.height(8.dp))
        InfoPropertyRow(label = "Key Protection", value = "Android Keystore / StrongBox")
        Spacer(modifier = Modifier.height(8.dp))
        InfoPropertyRow(label = "Architecture", value = "Clean Architecture / Layered MVVM")
        Spacer(modifier = Modifier.height(8.dp))
        InfoPropertyRow(label = "Privacy Guarantee", value = "100% On-Device • Zero Cloud Storage")

        Spacer(modifier = Modifier.height(14.dp))
        HairlineDivider()
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "SmartSpend • Private Digital Ledger\nDesigned and engineered for financial sovereignty.",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
            color = AtelierInkMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ==========================================
// REUSABLE HELPER UI COMPONENTS
// ==========================================

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AtelierHairline, RoundedCornerShape(6.dp)),
        shape = RoundedCornerShape(6.dp),
        color = AtelierCanvas
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionLabel(text = title)
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun ThemeOptionPill(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isSelected) AtelierPrimaryInk else AtelierSurfaceChalk
    val content = if (isSelected) AtelierCanvas else AtelierPrimaryInk
    val border = if (isSelected) AtelierPrimaryInk else AtelierHairline

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = bg,
        modifier = modifier
            .border(1.dp, border, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = content, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                ),
                color = content
            )
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold), color = AtelierPrimaryInk)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp), color = AtelierInkMuted)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = AtelierCanvas,
                checkedTrackColor = AtelierPrimaryInk,
                uncheckedThumbColor = AtelierInkMuted,
                uncheckedTrackColor = AtelierSurfaceChalk
            )
        )
    }
}

@Composable
private fun SettingActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = AtelierPrimaryInk, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold), color = AtelierPrimaryInk)
                Spacer(modifier = Modifier.height(1.dp))
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp), color = AtelierInkMuted)
            }
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AtelierInkMuted, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun InfoPropertyRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = AtelierInkMuted)
        Text(text = value, style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium), color = AtelierPrimaryInk)
    }
}

@Composable
private fun SpendingBarChart(
    items: List<BarChartItem>,
    modifier: Modifier = Modifier
) {
    val barColor = AtelierPrimaryInk
    val peakColor = AtelierCoral
    val baseLineColor = AtelierHairline

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val count = items.size
                if (count == 0) return@Canvas

                val barWidth = (size.width / (count * 2f)).coerceIn(12f, 32f)
                val spacing = (size.width - (barWidth * count)) / (count + 1)
                val chartHeight = size.height - 10f

                drawLine(
                    color = baseLineColor,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 2f
                )

                items.forEachIndexed { index, item ->
                    val x = spacing + index * (barWidth + spacing)
                    val barH = (item.percentage * chartHeight).coerceAtLeast(4f)
                    val y = size.height - barH

                    drawRoundRect(
                        color = if (item.isPeak) peakColor else barColor,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            items.forEach { item ->
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = if (item.isPeak) FontWeight.Bold else FontWeight.Normal,
                        lineHeight = 11.sp
                    ),
                    color = if (item.isPeak) AtelierCoral else AtelierInkMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun CategoryDonutChart(
    segments: List<DonutChartSegment>,
    totalAmount: BigDecimal,
    currency: String,
    modifier: Modifier = Modifier
) {
    val palette = listOf(
        AtelierAmber,
        AtelierCoral,
        AtelierPeriwinkle,
        AtelierSage,
        Color(0xFF8B5CF6),
        Color(0xFFEC4899),
        Color(0xFF06B6D4),
        Color(0xFFF59E0B)
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(130.dp)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 24f
                val radius = (size.minDimension - strokeWidth) / 2f
                val center = Offset(size.width / 2f, size.height / 2f)

                var startAngle = -90f
                segments.forEach { seg ->
                    val sweep = (seg.percentage / 100f) * 360f
                    val color = palette[seg.colorIndex % palette.size]

                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    )
                    startAngle += sweep
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "TOTAL",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                    color = AtelierInkMuted
                )
                Text(
                    text = MoneyUtils.format(totalAmount, currency),
                    fontFamily = NewsreaderFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AtelierPrimaryInk
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            segments.take(5).forEach { seg ->
                val color = palette[seg.colorIndex % palette.size]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = seg.categoryName,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = AtelierPrimaryInk,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = "${String.format("%.1f", seg.percentage)}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = AtelierInkMuted
                    )
                }
            }
        }
    }
}
