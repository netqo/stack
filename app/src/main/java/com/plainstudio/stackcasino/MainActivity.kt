package com.plainstudio.stackcasino

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.plainstudio.stackcasino.feature.splash.SplashUiState
import com.plainstudio.stackcasino.feature.splash.SplashViewModel
import com.plainstudio.stackcasino.ui.theme.StackcasinoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val splashViewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition {
            splashViewModel.uiState.value is SplashUiState.Resolving
        }
        enableEdgeToEdge()
        setContent {
            val state by splashViewModel.uiState.collectAsStateWithLifecycle()
            when (val current = state) {
                SplashUiState.Resolving ->
                    StackcasinoTheme {
                        // The system splash overlays this surface; once the
                        // ViewModel reports Ready we replace it with StackApp.
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background,
                        ) {}
                    }
                is SplashUiState.Ready ->
                    StackApp(startDestination = current.startDestination)
            }
        }
    }
}
