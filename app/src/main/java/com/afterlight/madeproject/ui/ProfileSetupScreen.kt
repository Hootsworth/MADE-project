package com.afterlight.madeproject.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.afterlight.madeproject.ui.components.SmoothButton
import com.afterlight.madeproject.ui.components.SmoothTextField
import com.afterlight.madeproject.ui.theme.*

@Composable
fun ProfileSetupScreen(
    onNext: () -> Unit,
    viewModel: ProfileSetupViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val interests = listOf("Design", "Tech", "Music", "Cinema", "Debate", "Career", "Sports")

    val avatarUrl = viewModel.externalApiService.diceBearAvatarUrl(
        state.name.ifBlank { "new-user" }
    )

    val schools = listOf(
        "School of Computer Science and Engineering",
        "School of Law",
        "School of Liberal Arts",
        "School of Continuing Education",
        "School of Allied Healthcare",
        "School of Business",
        "School of Film, Media and Creative Arts",
        "School of Economics and Public Policy",
        "School of Design and Innovation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Snow)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(top = 48.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Profile Setup", style = GatherTypography.displayLarge, color = Coal)
            Text(text = "Let's personalize your experience.", style = GatherTypography.bodyLarge, color = LightTextMuted)
        }

        // Smooth Avatar Preview
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Pearl.copy(alpha = 0.3f)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Avatar preview",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Pearl),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = "Your Avatar",
                    style = GatherTypography.labelMedium,
                    color = LightTextMuted
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Pearl),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "Primary Attributes",
                    style = GatherTypography.titleLarge,
                    color = Coal
                )

                SmoothTextField(value = state.name, onValueChange = viewModel::setName, label = "Full Name")
                SmoothTextField(value = state.year, onValueChange = viewModel::setYear, label = "Academic Year")

                Text(
                    text = "School",
                    style = GatherTypography.titleMedium,
                    color = Coal
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    schools.forEach { school ->
                        val isSelected = state.school == school
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { viewModel.setSchool(school) },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) Coal else Snow,
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Pearl)
                        ) {
                            Text(
                                text = school,
                                style = GatherTypography.bodyMedium,
                                color = if (isSelected) Snow else Coal,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(thickness = 1.dp, color = Pearl)

                Text(
                    text = "Interests",
                    style = GatherTypography.titleLarge,
                    color = Coal
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    interests.chunked(3).forEach { rowInterests ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowInterests.forEach { tag ->
                                val isSelected = state.interests.contains(tag)
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(CircleShape)
                                        .clickable { viewModel.toggleInterest(tag) },
                                    shape = CircleShape,
                                    color = if (isSelected) Coal else Pearl.copy(alpha = 0.4f)
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 12.dp)) {
                                        Text(
                                            text = tag,
                                            style = GatherTypography.labelMedium,
                                            color = if (isSelected) Snow else Coal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                SmoothButton(
                    text = if (state.saving) "Saving..." else "Continue",
                    onClick = { viewModel.saveProfile(onNext) },
                    containerColor = Copper,
                    contentColor = Snow,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.saving
                )
            }
        }

        AnimatedVisibility(visible = state.error != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Wine.copy(alpha = 0.1f)
            ) {
                Text(
                    text = "Error: ${state.error.orEmpty()}",
                    style = GatherTypography.bodyMedium,
                    color = Wine,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.padding(bottom = 32.dp))
    }
}