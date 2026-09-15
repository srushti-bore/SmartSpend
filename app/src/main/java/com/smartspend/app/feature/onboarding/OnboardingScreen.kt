package com.smartspend.app.feature.onboarding

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartspend.app.core.ui.components.DoubleHairlineRule
import com.smartspend.app.core.ui.components.HairlineDivider
import com.smartspend.app.core.ui.components.PatternLockView
import com.smartspend.app.core.ui.theme.AtelierAmber
import com.smartspend.app.core.ui.theme.AtelierAmberSubtle
import com.smartspend.app.core.ui.theme.AtelierCanvas
import com.smartspend.app.core.ui.theme.AtelierCoral
import com.smartspend.app.core.ui.theme.AtelierInkMuted
import com.smartspend.app.core.ui.theme.AtelierPrimaryInk
import com.smartspend.app.core.ui.theme.AtelierSage
import com.smartspend.app.core.ui.theme.AtelierSageSubtle
import com.smartspend.app.core.ui.theme.AtelierSurfaceChalk
import com.smartspend.app.core.ui.theme.NewsreaderFontFamily
import com.smartspend.app.domain.model.AuthType

@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    val restoreFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.restoreFromBackup(context, uri)
        }
    }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onOnboardingComplete()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AtelierCanvas)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to SmartSpend",
            fontFamily = NewsreaderFontFamily,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = AtelierPrimaryInk
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Your private, local-first intelligent financial ledger.",
            style = MaterialTheme.typography.bodyLarge,
            color = AtelierInkMuted
        )

        Spacer(modifier = Modifier.height(20.dp))
        DoubleHairlineRule()
        Spacer(modifier = Modifier.height(20.dp))

        // Restore Success Notification
        state.restoreSummary?.let { summary ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AtelierSageSubtle)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AtelierSage)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🎉 All data restored!",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = AtelierSage
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• ${summary.expensesCount} Expenses restored\n• ${summary.incomesCount} Income records restored\n• ${summary.categoriesCount} Categories restored\n• ${summary.accountsCount} Accounts restored\n• ${summary.savingsGoalsCount} Savings Goals restored",
                        style = MaterialTheme.typography.bodySmall,
                        color = AtelierPrimaryInk
                    )
                }
            }
        }

        // Profile Name Field
        OutlinedTextField(
            value = state.name,
            onValueChange = viewModel::onNameChange,
            label = { Text("Profile Name (e.g. Srushti)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Auth Type Selector
        Text(
            text = "Choose Primary Lock Method",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = AtelierPrimaryInk
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AuthType.values().forEach { type ->
                FilterChip(
                    selected = state.authType == type,
                    onClick = { viewModel.onAuthTypeChange(type) },
                    label = { Text(type.name) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AtelierPrimaryInk,
                        selectedLabelColor = AtelierCanvas
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Credential Input or Pattern Lock View
        if (state.authType == AuthType.PATTERN) {
            Surface(
                color = if (state.patternStep == 3) AtelierSageSubtle else AtelierSurfaceChalk,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = when (state.patternStep) {
                            1 -> "Step 1: Draw your unlock pattern"
                            2 -> "Step 2: Draw the pattern again to confirm"
                            else -> "✓ Pattern Confirmed"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = if (state.patternStep == 3) AtelierSage else AtelierPrimaryInk
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = state.patternHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = AtelierInkMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    PatternLockView(
                        size = 260.dp,
                        minDots = 4,
                        isError = state.isPatternError,
                        enabled = state.patternStep < 3,
                        onPatternStarted = viewModel::onPatternStarted,
                        onPatternCompleted = viewModel::onPatternCompleted
                    )

                    if (state.patternStep > 1) {
                        Spacer(modifier = Modifier.height(6.dp))
                        TextButton(onClick = viewModel::resetPattern) {
                            Text("Reset Pattern", color = AtelierAmber, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        } else {
            val keyboardType = if (state.authType == AuthType.PIN) KeyboardType.NumberPassword else KeyboardType.Password
            OutlinedTextField(
                value = state.credential,
                onValueChange = viewModel::onCredentialChange,
                label = { Text("Set ${state.authType.name}") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = state.confirmCredential,
                onValueChange = viewModel::onConfirmCredentialChange,
                label = { Text("Confirm ${state.authType.name}") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                shape = RoundedCornerShape(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Biometrics Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Enable Biometric Unlock",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = AtelierPrimaryInk
                )
                Text(
                    text = "Unlock ledger seamlessly with Fingerprint",
                    style = MaterialTheme.typography.bodySmall,
                    color = AtelierInkMuted
                )
            }
            Switch(
                checked = state.biometricEnabled,
                onCheckedChange = viewModel::onBiometricToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AtelierCanvas,
                    checkedTrackColor = AtelierAmber
                )
            )
        }

        if (state.errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = state.errorMessage ?: "",
                color = AtelierCoral,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Submit New Profile Button
        Button(
            onClick = viewModel::submit,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp),
            enabled = !state.isLoading && !state.isRestoring,
            colors = ButtonDefaults.buttonColors(
                containerColor = AtelierPrimaryInk,
                contentColor = AtelierCanvas
            )
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(color = AtelierCanvas, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = "Initialize Ledger & Start",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Divider with OR
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HairlineDivider(modifier = Modifier.weight(1f))
            Text(
                text = "OR",
                modifier = Modifier.padding(horizontal = 12.dp),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = AtelierInkMuted
            )
            HairlineDivider(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Restore Backup Button for Fresh APK / Reset Recovery
        OutlinedButton(
            onClick = { restoreFilePicker.launch(arrayOf("*/*")) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, AtelierAmber),
            enabled = !state.isLoading && !state.isRestoring,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AtelierPrimaryInk)
        ) {
            if (state.isRestoring) {
                CircularProgressIndicator(color = AtelierAmber, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Restoring Backup...",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = null,
                    tint = AtelierAmber,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Restore Backup (.enc / .smartspend)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
