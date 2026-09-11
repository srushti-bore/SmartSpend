package com.smartspend.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.smartspend.app.core.ui.theme.SmartSpendTheme
import com.smartspend.app.navigation.SmartSpendNavGraph
import dagger.hilt.android.AndroidEntryPoint

import androidx.lifecycle.lifecycleScope
import com.smartspend.app.core.datastore.PreferencesManager
import com.smartspend.app.domain.repository.ProfileRepository
import com.smartspend.app.domain.usecase.demo.SeedDemoDataUseCase
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var seedDemoDataUseCase: SeedDemoDataUseCase

    @Inject
    lateinit var preferencesManager: PreferencesManager

    @Inject
    lateinit var profileRepository: ProfileRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Protect financial screens against recents preview and screenshots in Release builds per SRS §7.1 & §24
        if (!BuildConfig.DEBUG) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }

        handleDemoSeedIntent(intent)

        val sharedImageUri: Uri? = if (intent?.action == Intent.ACTION_SEND && intent?.type?.startsWith("image/") == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(Intent.EXTRA_STREAM)
            }
        } else null

        setContent {
            val themeMode by preferencesManager.themeModeFlow.collectAsState(initial = "SYSTEM")
            val isDark = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            SmartSpendTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    SmartSpendNavGraph(
                        navController = navController,
                        sharedImageUri = sharedImageUri
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleDemoSeedIntent(intent)
    }

    private fun handleDemoSeedIntent(intent: Intent?) {
        if (intent == null) return
        val isSeedRequested = intent.getBooleanExtra("seed_demo", false) ||
                intent.action == "com.smartspend.app.ACTION_SEED_DEMO"
        if (isSeedRequested) {
            lifecycleScope.launch {
                try {
                    val activeId = preferencesManager.activeProfileIdFlow.firstOrNull()
                        ?: profileRepository.getAllProfiles().firstOrNull()?.firstOrNull()?.id
                    if (activeId != null) {
                        val result = seedDemoDataUseCase(activeId)
                        Timber.i("SeedDemoData completed with: $result")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to seed demo data via intent")
                }
            }
        }
    }
}
