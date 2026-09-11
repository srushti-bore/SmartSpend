package com.smartspend.app.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pattern
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.smartspend.app.core.ui.theme.AtelierPrimaryInk
import com.smartspend.app.core.ui.theme.AtelierSage
import com.smartspend.app.core.ui.theme.AtelierSageSubtle
import com.smartspend.app.core.ui.theme.AtelierSurfaceChalk
import com.smartspend.app.core.ui.theme.NewsreaderFontFamily
import com.smartspend.app.domain.model.AuthType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileMenuBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onLogout: () -> Unit,
    onAccountReset: () -> Unit,
    onNavigateToBackup: () -> Unit,
    viewModel: ProfileMenuViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    // 1. Reset / Change Security Credential Modal Dialog
    if (state.isResetPinDialogOpen) {
        AlertDialog(
            onDismissRequest = viewModel::closeResetPinDialog,
            containerColor = AtelierCanvas,
            title = {
                Text(
                    text = "Security & Access Lock",
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

                    // Lock Method Switcher Chips
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

    // 2. Delete Account Confirmation Modal
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
                            onDismiss()
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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AtelierCanvas,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(AtelierHairline)
            )
        }
    ) {
        val profile = state.activeProfile
        val profileName = profile?.name?.ifBlank { "Srushti" } ?: "Srushti"
        val monogram = profileName.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("").ifBlank { "S" }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(AtelierSurfaceChalk)
                            .border(1.5.dp, AtelierAmber, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = monogram,
                            fontFamily = NewsreaderFontFamily,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = AtelierPrimaryInk
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = profileName,
                            fontFamily = NewsreaderFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = AtelierPrimaryInk
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "PRIMARY LEDGER • FOLIO NO. 412",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = AtelierInkMuted
                        )
                    }
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = AtelierPrimaryInk,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (state.pinSuccessMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = AtelierSageSubtle,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = state.pinSuccessMessage ?: "",
                        color = AtelierSage,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            DoubleHairlineRule()
            Spacer(modifier = Modifier.height(18.dp))

            // 1. THEME MODE SELECTOR (LIGHT / DARK / SYSTEM)
            SectionLabel(text = "Appearance & Interface Theme")
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeOptionButton(
                    title = "Light",
                    icon = Icons.Default.Brightness7,
                    isSelected = state.themeMode == "LIGHT",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setThemeMode("LIGHT") }
                )
                ThemeOptionButton(
                    title = "Dark",
                    icon = Icons.Default.Brightness4,
                    isSelected = state.themeMode == "DARK",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setThemeMode("DARK") }
                )
                ThemeOptionButton(
                    title = "System",
                    icon = Icons.Default.BrightnessAuto,
                    isSelected = state.themeMode == "SYSTEM",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setThemeMode("SYSTEM") }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            HairlineDivider()
            Spacer(modifier = Modifier.height(18.dp))

            // 2. SECURITY & AUTHENTICATION ACTIONS
            SectionLabel(text = "Passbook Security & Credentials")
            Spacer(modifier = Modifier.height(10.dp))

            ProfileActionButton(
                title = "Security & Passcode Lock",
                subtitle = "Configure PIN, Pattern, or Password protection",
                icon = Icons.Default.Key,
                iconTint = AtelierAmber,
                onClick = viewModel::openResetPinDialog
            )

            Spacer(modifier = Modifier.height(8.dp))

            ProfileActionButton(
                title = "Backup & Hardware Export",
                subtitle = "Manage encrypted JSON passbook archives",
                icon = Icons.Default.Storage,
                iconTint = AtelierPeriwinkle,
                onClick = {
                    onDismiss()
                    onNavigateToBackup()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            ProfileActionButton(
                title = "Lock & Log Out",
                subtitle = "End current session and lock passbook",
                icon = Icons.Default.Lock,
                iconTint = AtelierPrimaryInk,
                onClick = {
                    viewModel.logout {
                        onDismiss()
                        onLogout()
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))
            HairlineDivider()
            Spacer(modifier = Modifier.height(18.dp))

            // 3. DANGER ZONE: DATA WIPE & ACCOUNT RESET
            SectionLabel(text = "Danger Zone", color = AtelierCoral)
            Spacer(modifier = Modifier.height(10.dp))

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
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = AtelierCoral,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Delete Account & Wipe Database",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = AtelierCoral
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Permanently reset app to fresh onboarding state",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = AtelierInkMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeOptionButton(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
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
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = content,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                ),
                color = content
            )
        }
    }
}

@Composable
private fun ProfileActionButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(AtelierSurfaceChalk),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = AtelierPrimaryInk
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = AtelierInkMuted
                )
            }
        }
    }
}
