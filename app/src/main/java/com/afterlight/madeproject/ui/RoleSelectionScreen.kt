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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.afterlight.madeproject.domain.model.UserRole
import com.afterlight.madeproject.ui.components.SmoothButton
import com.afterlight.madeproject.ui.theme.*

@Composable
fun RoleSelectionScreen(
    onFinish: () -> Unit,
    viewModel: ProfileSetupViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Snow)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Column(
            modifier = Modifier.padding(top = 48.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Access Role", style = GatherTypography.displayLarge, color = Coal)
            Text(text = "Choose your permission level.", style = GatherTypography.bodyLarge, color = LightTextMuted)
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
                    text = "Operational Mode",
                    style = GatherTypography.titleLarge,
                    color = Coal
                )

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    val roles = listOf(
                        UserRole.STUDENT to "Student Participant" to "Discover & RSVP to campus events",
                        UserRole.HOST to "Event Organizer" to "Create & manage editorial editions"
                    )

                    roles.forEach { (rolePair, desc) ->
                        val (role, title) = rolePair
                        val isSelected = state.role == role

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .clickable(enabled = !state.saving) { viewModel.setRole(role) },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) Sand.copy(alpha = 0.2f) else Pearl.copy(alpha = 0.2f),
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) Coal else Pearl.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (role == UserRole.STUDENT) Icons.Default.Search else Icons.Default.Add,
                                            contentDescription = null,
                                            tint = if (isSelected) Snow else Coal
                                        )
                                    }
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = title,
                                        style = GatherTypography.titleLarge,
                                        color = Coal
                                    )
                                    Text(
                                        text = desc,
                                        style = GatherTypography.bodyMedium,
                                        color = LightTextMuted
                                    )
                                }
                            }
                        }
                    }
                }

                SmoothButton(
                    text = if (state.saving) "Initializing..." else "Initialize App",
                    onClick = { viewModel.saveProfile(onFinish) },
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
    }
}