package com.afterlight.madeproject.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.afterlight.madeproject.domain.model.ThemeMode
import com.afterlight.madeproject.ui.theme.Coal
import com.afterlight.madeproject.ui.theme.GatherTypography
import com.afterlight.madeproject.ui.theme.LightTextMuted
import com.afterlight.madeproject.ui.theme.Moss
import com.afterlight.madeproject.ui.theme.Pearl
import com.afterlight.madeproject.ui.theme.Snow

@Composable
fun SettingsScreen(
    onAccountClick: () -> Unit,
    onAiSettingsClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Snow)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Column(
            modifier = Modifier.padding(top = 40.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Settings", style = GatherTypography.displayLarge, color = Coal)
            Text(
                text = "Manage your preferences and configurations.",
                style = GatherTypography.bodyLarge,
                color = LightTextMuted
            )
        }

        SettingsGroup(title = "Account & Profile") {
            SettingsRow(
                icon = Icons.Outlined.PersonOutline,
                title = "Account Center",
                subtitle = "Profile, stats, shortcuts, and sign out",
                onClick = onAccountClick
            ) {
                Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Coal.copy(alpha = 0.5f))
            }
        }

        SettingsGroup(title = "App Preferences") {
            // Moved ThemePicker below the text for a cleaner, blockier layout
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp), // Sharper, geometric icon container
                        color = Pearl.copy(alpha = 0.6f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Palette,
                                contentDescription = null,
                                tint = Coal,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(text = "Theme Mode", style = GatherTypography.titleMedium, color = Coal)
                        Text(
                            text = when (state.themeMode) {
                                ThemeMode.SYSTEM -> "Follow the device setting"
                                ThemeMode.LIGHT -> "Bright, editorial, paper-like"
                                ThemeMode.DARK -> "Deep, immersive, low-light"
                            },
                            style = GatherTypography.bodyMedium,
                            color = LightTextMuted
                        )
                    }
                }

                ThemeModePicker(
                    selected = state.themeMode,
                    onSelected = viewModel::setThemeMode
                )
            }

            HorizontalDivider(color = Pearl.copy(alpha = 0.8f), thickness = 1.dp)

            SettingsRow(
                icon = Icons.Outlined.Visibility,
                title = "Show Onboarding",
                subtitle = "Display welcome screens on launch",
                onClick = { viewModel.setOnboardingDone(state.onboardingDone) }
            ) {
                Switch(
                    checked = !state.onboardingDone,
                    onCheckedChange = { viewModel.setOnboardingDone(!it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Snow,
                        checkedTrackColor = Coal, // High contrast active state
                        uncheckedThumbColor = Coal.copy(alpha = 0.7f),
                        uncheckedTrackColor = Pearl,
                        uncheckedBorderColor = Coal.copy(alpha = 0.2f)
                    )
                )
            }
        }

        SettingsGroup(title = "Intelligence") {
            SettingsRow(
                icon = Icons.Outlined.AutoAwesome,
                title = "AI Assistant",
                subtitle = "Manage providers, models, and API keys",
                onClick = onAiSettingsClick
            ) {
                Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Coal.copy(alpha = 0.5f))
            }
        }

        Spacer(modifier = Modifier.height(88.dp))
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title.uppercase(),
            style = GatherTypography.labelMedium,
            color = Coal, // Stronger header contrast
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp), // Sharper, more structural corners
            colors = CardDefaults.cardColors(containerColor = Snow), // Flatten background
            border = BorderStroke(1.dp, Coal.copy(alpha = 0.12f)), // Crisp borders instead of shadows
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Removed elevation
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null, onClick = onClick ?: {})
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp), // Geometric icon background
            color = Pearl.copy(alpha = 0.6f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Coal,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = title, style = GatherTypography.titleMedium, color = Coal)
            if (subtitle != null) {
                Text(text = subtitle, style = GatherTypography.bodyMedium, color = LightTextMuted)
            }
        }

        trailing()
    }
}

@Composable
private fun ThemeModePicker(
    selected: ThemeMode,
    onSelected: (ThemeMode) -> Unit
) {
    // Segmented control style for higher structural impact
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Pearl.copy(alpha = 0.4f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ThemeModePickerChip(
            text = "System",
            selected = selected == ThemeMode.SYSTEM,
            onClick = { onSelected(ThemeMode.SYSTEM) },
            modifier = Modifier.weight(1f)
        )
        ThemeModePickerChip(
            text = "Light",
            selected = selected == ThemeMode.LIGHT,
            onClick = { onSelected(ThemeMode.LIGHT) },
            modifier = Modifier.weight(1f)
        )
        ThemeModePickerChip(
            text = "Dark",
            selected = selected == ThemeMode.DARK,
            onClick = { onSelected(ThemeMode.DARK) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ThemeModePickerChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(36.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(6.dp),
        color = if (selected) Coal else Color.Transparent,
        shadowElevation = if (selected) 2.dp else 0.dp // Updated from 'elevation'
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = GatherTypography.labelMedium,
                color = if (selected) Snow else Coal.copy(alpha = 0.6f)
            )
        }
    }
}