package com.afterlight.madeproject.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.afterlight.madeproject.ui.components.BrutalistButton
import com.afterlight.madeproject.ui.components.BrutalistTextField
import com.afterlight.madeproject.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileSetupScreen(
    onNext: () -> Unit,
    viewModel: ProfileSetupViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val interests = listOf("Design", "Tech", "Music", "Cinema", "Debate", "Career", "Sports")

    // DiceBear avatar preview (API #1) — seeded by the user's name
    val avatarUrl = viewModel.externalApiService.diceBearAvatarUrl(
        state.name.ifBlank { "new-user" }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Snow)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Column(
            modifier = Modifier.padding(top = 48.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "PROFILE REGISTRY.", style = GatherTypography.displayLarge, color = Coal)
            Text(text = "IDENTITY SETTINGS", style = GatherTypography.labelMedium, color = LightTextMuted)
        }

        // DiceBear Avatar Preview — changes dynamically as user types their name
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neoBrutalism(backgroundColor = Pearl, shadowOffset = 4.dp)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Avatar preview",
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .border(3.dp, Coal, CircleShape),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = "YOUR AVATAR",
                    style = GatherTypography.labelSmall,
                    color = LightTextMuted
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neoBrutalism(backgroundColor = Pearl, shadowOffset = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "PRIMARY ATTRIBUTES",
                    style = GatherTypography.labelLarge,
                    color = Coal
                )

                BrutalistTextField(value = state.name, onValueChange = viewModel::setName, label = "FULL NAME")
                BrutalistTextField(value = state.year, onValueChange = viewModel::setYear, label = "ACADEMIC YEAR")
                BrutalistTextField(value = state.department, onValueChange = viewModel::setDepartment, label = "DEPARTMENT")

                HorizontalDivider(thickness = 4.dp, color = Coal)

                Text(
                    text = "INTEREST VECTORS",
                    style = GatherTypography.labelLarge,
                    color = Coal
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    interests.forEach { tag ->
                        val isSelected = state.interests.contains(tag)
                        Box(
                            modifier = Modifier
                                .neoBrutalism(
                                    backgroundColor = if (isSelected) Coal else Snow,
                                    shadowOffset = 2.dp,
                                    borderWidth = 2.dp
                                )
                                .clickable { viewModel.toggleInterest(tag) }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = tag.uppercase(),
                                style = GatherTypography.labelMedium,
                                color = if (isSelected) Snow else Coal
                            )
                        }
                    }
                }

                BrutalistButton(
                    text = "CONTINUE TO ROLE",
                    onClick = { viewModel.saveProfile(onNext) },
                    backgroundColor = Copper,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        AnimatedVisibility(visible = state.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neoBrutalism(backgroundColor = Wine, shadowOffset = 4.dp)
                    .padding(16.dp)
            ) {
                Text(
                    text = "VALIDATION ERROR: ${state.error.orEmpty()}",
                    style = GatherTypography.labelMedium,
                    color = Snow
                )
            }
        }

        Spacer(modifier = Modifier.padding(bottom = 32.dp))
    }
}