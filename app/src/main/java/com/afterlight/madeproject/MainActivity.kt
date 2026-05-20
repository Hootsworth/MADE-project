package com.afterlight.madeproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.afterlight.madeproject.ui.GatherApp
import com.afterlight.madeproject.ui.ThemeViewModel
import com.afterlight.madeproject.ui.theme.GatherTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val themeMode by themeViewModel.themeMode.collectAsStateWithLifecycle()
            val darkTheme = when (themeMode) {
                com.afterlight.madeproject.domain.model.ThemeMode.SYSTEM -> isSystemInDarkTheme()
                com.afterlight.madeproject.domain.model.ThemeMode.LIGHT -> false
                com.afterlight.madeproject.domain.model.ThemeMode.DARK -> true
                else -> isSystemInDarkTheme()
            }
            val view = LocalView.current

            SideEffect {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }

            GatherTheme(darkTheme = darkTheme) {
                GatherApp()
            }
        }
    }
}