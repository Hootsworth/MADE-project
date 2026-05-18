package com.afterlight.madeproject.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.afterlight.madeproject.domain.model.AiProvider
import com.afterlight.madeproject.ui.components.BrutalistButton
import com.afterlight.madeproject.ui.components.BrutalistTextField
import com.afterlight.madeproject.ui.theme.Coal
import com.afterlight.madeproject.ui.theme.Copper
import com.afterlight.madeproject.ui.theme.GatherTypography
import com.afterlight.madeproject.ui.theme.LightGlass
import com.afterlight.madeproject.ui.theme.LightTextMuted
import com.afterlight.madeproject.ui.theme.Moss
import com.afterlight.madeproject.ui.theme.Pearl
import com.afterlight.madeproject.ui.theme.Sand
import com.afterlight.madeproject.ui.theme.Snow
import com.afterlight.madeproject.ui.theme.neoBrutalism

@Composable
fun SettingsScreen(
    onAccountClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isOpenAi = state.provider == AiProvider.OPENAI

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Snow)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(top = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "CONTROL ROOM", style = GatherTypography.displayLarge, color = Coal)
            Text(
                text = "Tune the assistant stack, keep credentials organized, and jump into account controls.",
                style = GatherTypography.bodyLarge,
                color = LightTextMuted
            )
        }

        SettingsPanel(title = "Account") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neoBrutalism(backgroundColor = Pearl, shadowOffset = 3.dp)
                    .clickable(onClick = onAccountClick)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.PersonOutline, contentDescription = null, tint = Coal)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "ACCOUNT CENTER", style = GatherTypography.labelLarge, color = Coal)
                            Text(text = "Open profile, stats, shortcuts, and sign-out tools.", style = GatherTypography.bodyMedium, color = LightTextMuted)
                        }
                    }
                    Icon(Icons.Outlined.ChevronRight, contentDescription = "Account Center", tint = Coal)
                }
            }
        }

        SettingsPanel(title = "AI Provider") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProviderCard(
                    title = "OpenAI",
                    body = "Balanced defaults for summaries and event polish.",
                    selected = isOpenAi,
                    accent = Copper,
                    onClick = { viewModel.setProvider(AiProvider.OPENAI) },
                    modifier = Modifier.weight(1f)
                )
                ProviderCard(
                    title = "Gemini",
                    body = "Alternative model path for experimentation and fallback.",
                    selected = !isOpenAi,
                    accent = Moss,
                    onClick = { viewModel.setProvider(AiProvider.GEMINI) },
                    modifier = Modifier.weight(1f)
                )
            }

            BrutalistTextField(
                value = state.model,
                onValueChange = viewModel::setModel,
                label = "MODEL IDENTIFIER"
            )
        }

        SettingsPanel(title = "Credentials") {
            Text(
                text = if (isOpenAi) {
                    "OpenAI is active. Keep both keys handy if you want a quick provider switch later."
                } else {
                    "Gemini is active. OpenAI can still stay populated as a standby provider."
                },
                style = GatherTypography.bodyMedium,
                color = LightTextMuted
            )

            BrutalistTextField(
                value = state.openAiApiKey,
                onValueChange = viewModel::setOpenAiKey,
                label = "OPENAI API KEY"
            )

            BrutalistTextField(
                value = state.geminiApiKey,
                onValueChange = viewModel::setGeminiKey,
                label = "GEMINI API KEY"
            )
        }

        SettingsPanel(title = "Commit") {
            Text(
                text = "Save the current provider, model, and key configuration to local settings.",
                style = GatherTypography.bodyMedium,
                color = LightTextMuted
            )

            BrutalistButton(
                text = "Commit Changes",
                onClick = viewModel::save,
                backgroundColor = Moss,
                modifier = Modifier.fillMaxWidth()
            )

            state.status?.let { status ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neoBrutalism(backgroundColor = Sand, shadowOffset = 3.dp)
                        .padding(14.dp)
                ) {
                    Text(text = status.uppercase(), style = GatherTypography.labelMedium, color = Coal)
                }
            }
        }

        Spacer(modifier = Modifier.padding(bottom = 100.dp))
    }
}

@Composable
private fun SettingsPanel(
    title: String,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neoBrutalism(backgroundColor = Snow, shadowOffset = 6.dp)
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(text = title.uppercase(), style = GatherTypography.labelLarge, color = Coal)
            content()
        }
    }
}

@Composable
private fun ProviderCard(
    title: String,
    body: String,
    selected: Boolean,
    accent: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .neoBrutalism(
                backgroundColor = if (selected) accent else LightGlass,
                shadowOffset = 3.dp
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = title.uppercase(), style = GatherTypography.labelLarge, color = Coal)
            Text(text = body, style = GatherTypography.bodyMedium, color = Coal)
            Text(
                text = if (selected) "ACTIVE" else "TAP TO ACTIVATE",
                style = GatherTypography.labelMedium,
                color = Coal
            )
        }
    }
}
