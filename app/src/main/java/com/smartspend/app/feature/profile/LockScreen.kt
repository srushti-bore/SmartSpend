package com.smartspend.app.feature.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartspend.app.core.common.BiometricAuthHelper
import com.smartspend.app.core.ui.components.LoadingState
import com.smartspend.app.core.ui.components.PatternLockView
import com.smartspend.app.core.ui.theme.AtelierAmber
import com.smartspend.app.core.ui.theme.AtelierAmberSubtle
import com.smartspend.app.core.ui.theme.AtelierCanvas
import com.smartspend.app.core.ui.theme.AtelierCoral
import com.smartspend.app.core.ui.theme.AtelierHairline
import com.smartspend.app.core.ui.theme.AtelierInkMuted
import com.smartspend.app.core.ui.theme.AtelierPrimaryInk
import com.smartspend.app.core.ui.theme.AtelierSage
import com.smartspend.app.core.ui.theme.AtelierSurfaceChalk
import com.smartspend.app.core.ui.theme.NewsreaderFontFamily
import com.smartspend.app.domain.model.AuthType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockScreen(
    onUnlockSuccess: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    viewModel: LockViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isUnlocked) {
        if (state.isUnlocked) {
            onUnlockSuccess()
        }
    }

    LaunchedEffect(state.isLoading, state.activeProfile, state.allProfiles) {
        if (!state.isLoading && state.activeProfile == null && state.allProfiles.isEmpty()) {
            onNavigateToOnboarding()
        }
    }

    // Auto-prompt Android standard Biometric on screen load if profile has biometric enabled
    val activeProfile = state.activeProfile
    LaunchedEffect(activeProfile?.id, activeProfile?.biometricEnabled) {
        if (activeProfile != null && activeProfile.biometricEnabled && activity != null) {
            if (BiometricAuthHelper.canAuthenticate(activity)) {
                BiometricAuthHelper.authenticate(
                    activity = activity,
                    title = "Unlock SmartSpend",
                    subtitle = "Verify fingerprint, face, or device PIN/pattern for ${activeProfile.name}",
                    onSuccess = viewModel::onBiometricSuccess,
                    onError = { _, _ -> },
                    onFailed = { }
                )
            }
        }
    }

    if (state.isLoading) {
        LoadingState(message = "Checking authentication...")
        return
    }

    // Add Profile Dialog
    if (state.isAddUserDialogOpen) {
        AlertDialog(
            onDismissRequest = viewModel::closeAddUserDialog,
            containerColor = AtelierCanvas,
            title = {
                Text(
                    text = "Add New User Profile",
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
                        text = "Create an independent ledger profile with its own transactions, accounts, and passbook lock.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AtelierInkMuted
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = state.newUserName,
                        onValueChange = viewModel::onNewUserNameChange,
                        label = { Text("User / Profile Name") },
                        placeholder = { Text("e.g., Rohan, Family, Business") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Lock Method Switcher Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AuthType.values().forEach { type ->
                            FilterChip(
                                selected = state.newUserAuthType == type,
                                onClick = { viewModel.onNewUserAuthTypeSelected(type) },
                                label = { Text(type.name, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AtelierPrimaryInk,
                                    selectedLabelColor = AtelierCanvas
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val keyboardType = if (state.newUserAuthType == AuthType.PIN) KeyboardType.NumberPassword else KeyboardType.Password
                    OutlinedTextField(
                        value = state.newUserCredential,
                        onValueChange = viewModel::onNewUserCredentialChange,
                        label = { Text("Set ${state.newUserAuthType.name}") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = state.newUserConfirmCredential,
                        onValueChange = viewModel::onNewUserConfirmCredentialChange,
                        label = { Text("Confirm ${state.newUserAuthType.name}") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Enable Biometric Unlock",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AtelierPrimaryInk
                        )
                        Switch(
                            checked = state.newUserBiometric,
                            onCheckedChange = viewModel::onNewUserBiometricToggle,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AtelierCanvas,
                                checkedTrackColor = AtelierPrimaryInk
                            )
                        )
                    }

                    if (!state.newUserErrorMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = state.newUserErrorMessage ?: "",
                            color = AtelierCoral,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = viewModel::createNewUser,
                    enabled = !state.isCreatingUser,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AtelierPrimaryInk,
                        contentColor = AtelierCanvas
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    if (state.isCreatingUser) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = AtelierCanvas, strokeWidth = 2.dp)
                    } else {
                        Text("Create Profile")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::closeAddUserDialog) {
                    Text("Cancel", color = AtelierInkMuted)
                }
            }
        )
    }

    val profile = state.activeProfile ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AtelierCanvas)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 1. Multi-Profile Switcher Row
            if (state.allProfiles.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    state.allProfiles.forEach { p ->
                        val isSelected = p.id == profile.id
                        val pMonogram = p.name.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("").ifBlank { "U" }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) AtelierPrimaryInk else AtelierSurfaceChalk,
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) AtelierAmber else AtelierHairline,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable { viewModel.selectProfile(p) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) AtelierAmber else AtelierHairline),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = pMonogram,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) AtelierPrimaryInk else AtelierInkMuted
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = p.name,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) AtelierCanvas else AtelierPrimaryInk
                                )
                            }
                        }
                    }

                    // Add User Button
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = AtelierSurfaceChalk,
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .border(1.dp, AtelierHairline, RoundedCornerShape(20.dp))
                            .clickable { viewModel.openAddUserDialog() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add User",
                                tint = AtelierPrimaryInk,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Add User",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = AtelierPrimaryInk
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Lock Header Icon & Title
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(44.dp),
                tint = AtelierAmber
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Welcome Back, ${profile.name}",
                fontFamily = NewsreaderFontFamily,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AtelierPrimaryInk
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = when (profile.primaryAuthType) {
                    AuthType.PATTERN -> "Draw pattern to unlock"
                    AuthType.PIN -> "Enter 4-digit PIN"
                    AuthType.PASSWORD -> "Enter password to unlock"
                },
                style = MaterialTheme.typography.bodySmall,
                color = AtelierInkMuted
            )

            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.errorMessage ?: "",
                    color = AtelierCoral,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Lock Method Views (PIN Keypad / Pattern / Password)
        when (profile.primaryAuthType) {
            AuthType.PIN -> {
                PinLockContent(
                    pinInput = state.credentialInput,
                    isError = state.errorMessage != null,
                    biometricEnabled = profile.biometricEnabled,
                    onDigitPress = viewModel::onPinDigit,
                    onBackspace = viewModel::onPinBackspace,
                    onBiometricClick = {
                        if (activity != null) {
                            BiometricAuthHelper.authenticate(
                                activity = activity,
                                title = "Unlock SmartSpend",
                                subtitle = "Authenticate as ${profile.name}",
                                onSuccess = viewModel::onBiometricSuccess
                            )
                        }
                    }
                )
            }
            AuthType.PATTERN -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PatternLockView(
                        size = 280.dp,
                        minDots = 4,
                        isError = state.isPatternError,
                        onPatternStarted = viewModel::onPatternStarted,
                        onPatternCompleted = viewModel::onPatternCompleted
                    )

                    if (profile.biometricEnabled) {
                        Spacer(modifier = Modifier.height(16.dp))
                        BiometricButton(
                            onClick = {
                                if (activity != null) {
                                    BiometricAuthHelper.authenticate(
                                        activity = activity,
                                        title = "Unlock SmartSpend",
                                        subtitle = "Authenticate as ${profile.name}",
                                        onSuccess = viewModel::onBiometricSuccess
                                    )
                                }
                            }
                        )
                    }
                }
            }
            AuthType.PASSWORD -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = state.credentialInput,
                        onValueChange = viewModel::onCredentialChange,
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(0.85f),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = viewModel::unlock,
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AtelierPrimaryInk,
                            contentColor = AtelierCanvas
                        )
                    ) {
                        Text("Unlock", fontWeight = FontWeight.SemiBold)
                    }

                    if (profile.biometricEnabled) {
                        Spacer(modifier = Modifier.height(16.dp))
                        BiometricButton(
                            onClick = {
                                if (activity != null) {
                                    BiometricAuthHelper.authenticate(
                                        activity = activity,
                                        title = "Unlock SmartSpend",
                                        subtitle = "Authenticate as ${profile.name}",
                                        onSuccess = viewModel::onBiometricSuccess
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Standard Android PIN Keypad UI with Animated Dots & Dialpad
 */
@Composable
private fun PinLockContent(
    pinInput: String,
    isError: Boolean,
    biometricEnabled: Boolean,
    onDigitPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onBiometricClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // PIN Indicator Dots (4 dots standard)
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            val totalDots = 4
            for (i in 0 until totalDots) {
                val isFilled = i < pinInput.length
                val dotScale by animateFloatAsState(
                    targetValue = if (isFilled) 1.25f else 1.0f,
                    animationSpec = tween(150),
                    label = "dot_scale"
                )

                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .scale(dotScale)
                        .clip(CircleShape)
                        .background(
                            when {
                                isError -> AtelierCoral
                                isFilled -> AtelierPrimaryInk
                                else -> AtelierHairline
                            }
                        )
                        .border(
                            width = 1.dp,
                            color = if (isError) AtelierCoral else AtelierPrimaryInk,
                            shape = CircleShape
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Android Standard Numeric Dialpad
        val rows = listOf(
            listOf("1" to "", "2" to "ABC", "3" to "DEF"),
            listOf("4" to "GHI", "5" to "JKL", "6" to "MNO"),
            listOf("7" to "PQRS", "8" to "TUV", "9" to "WXYZ"),
            listOf("BIO" to "", "0" to "+", "DEL" to "")
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            rows.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    row.forEach { (key, subText) ->
                        when (key) {
                            "BIO" -> {
                                if (biometricEnabled) {
                                    Surface(
                                        shape = CircleShape,
                                        color = AtelierSurfaceChalk,
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clip(CircleShape)
                                            .clickable(onClick = onBiometricClick)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Fingerprint,
                                                contentDescription = "Biometric Unlock",
                                                tint = AtelierAmber,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.size(68.dp))
                                }
                            }
                            "DEL" -> {
                                Surface(
                                    shape = CircleShape,
                                    color = AtelierSurfaceChalk,
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .clickable(onClick = onBackspace)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Backspace,
                                            contentDescription = "Backspace",
                                            tint = AtelierPrimaryInk,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                            else -> {
                                Surface(
                                    shape = CircleShape,
                                    color = AtelierSurfaceChalk,
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, AtelierHairline, CircleShape)
                                        .clickable { onDigitPress(key) }
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Text(
                                            text = key,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AtelierPrimaryInk
                                        )
                                        if (subText.isNotEmpty()) {
                                            Text(
                                                text = subText,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = AtelierInkMuted,
                                                letterSpacing = 1.sp
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
    }
}

@Composable
private fun BiometricButton(onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = AtelierSurfaceChalk,
        modifier = Modifier
            .border(1.dp, AtelierAmber, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = null,
                tint = AtelierAmber,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Unlock with Biometrics",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = AtelierPrimaryInk
            )
        }
    }
}
