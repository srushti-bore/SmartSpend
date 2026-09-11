package com.smartspend.app.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartspend.app.core.ui.components.LoadingState
import com.smartspend.app.core.ui.theme.StatusDanger
import com.smartspend.app.domain.model.AuthType

@Composable
fun LockScreen(
    onUnlockSuccess: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    viewModel: LockViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isUnlocked) {
        if (state.isUnlocked) {
            onUnlockSuccess()
        }
    }

    LaunchedEffect(state.isLoading, state.activeProfile) {
        if (!state.isLoading && state.activeProfile == null) {
            onNavigateToOnboarding()
        }
    }

    if (state.isLoading) {
        LoadingState(message = "Checking authentication...")
        return
    }

    val profile = state.activeProfile ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Welcome Back, ${profile.name}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Enter your ${profile.primaryAuthType.name} to unlock SmartSpend",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        val keyboardType = if (profile.primaryAuthType == AuthType.PIN) KeyboardType.NumberPassword else KeyboardType.Password
        OutlinedTextField(
            value = state.credentialInput,
            onValueChange = viewModel::onCredentialChange,
            label = { Text(profile.primaryAuthType.name) },
            modifier = Modifier.fillMaxWidth(0.85f),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(12.dp)
        )

        if (state.errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = state.errorMessage ?: "",
                color = StatusDanger,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = viewModel::unlock,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Unlock", fontWeight = FontWeight.SemiBold)
        }

        if (profile.biometricEnabled) {
            Spacer(modifier = Modifier.height(20.dp))
            IconButton(
                onClick = viewModel::onBiometricSuccess,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Unlock with Biometrics",
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
