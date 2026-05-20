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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.afterlight.madeproject.domain.model.AiProvider
import com.afterlight.madeproject.ui.theme.Coal
import com.afterlight.madeproject.ui.theme.Copper
import com.afterlight.madeproject.ui.theme.GatherTypography
import com.afterlight.madeproject.ui.theme.LightTextMuted
import com.afterlight.madeproject.ui.theme.Moss
import com.afterlight.madeproject.ui.theme.Pearl
import com.afterlight.madeproject.ui.theme.Sand
import com.afterlight.madeproject.ui.theme.Snow

@Composable
fun AiSettingsScreen(
    onBackClick: () -> Unit,
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
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "AI Settings", style = GatherTypography.displayLarge, color = Coal)
                Text(
                    text = "Configure your intelligence layer.",
                    style = GatherTypography.bodyLarge,
                    color = LightTextMuted
                )
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Pearl)
                    .clickable { viewModel.save(onSaved = onBackClick) },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.ChevronLeft, contentDescription = "Back", tint = Coal)
            }
        }

        SettingsPanel(title = "Provider") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProviderCard(
                    title = "OpenAI",
                    body = "Balanced defaults",
                    selected = isOpenAi,
                    accent = Copper,
                    onClick = { viewModel.setProvider(AiProvider.OPENAI) },
                    modifier = Modifier.weight(1f)
                )
                ProviderCard(
                    title = "Gemini",
                    body = "Google's models",
                    selected = !isOpenAi,
                    accent = Moss,
                    onClick = { viewModel.setProvider(AiProvider.GEMINI) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        SettingsPanel(title = "Model & Credentials") {
            SmoothTextField(
                value = state.model,
                onValueChange = viewModel::setModel,
                label = "Model Identifier"
            )

            SmoothTextField(
                value = state.openAiApiKey,
                onValueChange = viewModel::setOpenAiKey,
                label = "OpenAI API Key"
            )

            SmoothTextField(
                value = state.geminiApiKey,
                onValueChange = viewModel::setGeminiKey,
                label = "Gemini API Key"
            )
        }

        SettingsPanel(title = "System") {
            Text(
                text = "Keys are encrypted and stored locally on your device hardware.",
                style = GatherTypography.bodyMedium,
                color = LightTextMuted
            )

            state.status?.let { status ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Sand.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = status,
                        style = GatherTypography.labelMedium,
                        color = Coal,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            SmoothButton(
                text = "Save Configuration",
                onClick = { viewModel.save() },
                containerColor = Coal,
                contentColor = Snow,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SettingsPanel(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Pearl),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(text = title, style = GatherTypography.titleLarge, color = Coal)
            content()
        }
    }
}

@Composable
private fun ProviderCard(
    title: String,
    body: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (selected) accent.copy(alpha = 0.1f) else Pearl.copy(alpha = 0.1f)

    Surface(
        modifier = modifier
            .height(140.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = containerColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = title, style = GatherTypography.labelLarge, color = Coal)
                Text(text = body, style = GatherTypography.bodyMedium, color = LightTextMuted)
            }
            Text(
                text = if (selected) "Active" else "Select",
                style = GatherTypography.labelMedium,
                color = if (selected) accent else LightTextMuted
            )
        }
    }
}

@Composable
private fun SmoothTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = GatherTypography.bodyMedium) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedLabelColor = Coal,
            unfocusedLabelColor = LightTextMuted
        )
    )
}