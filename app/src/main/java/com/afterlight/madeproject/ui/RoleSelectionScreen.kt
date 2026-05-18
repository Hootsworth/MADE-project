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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.afterlight.madeproject.domain.model.UserRole
import com.afterlight.madeproject.ui.components.BrutalistButton
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
            Text(text = "ACCESS ROLE.", style = GatherTypography.displayLarge, color = Coal)
            Text(text = "PERMISSION LEVEL", style = GatherTypography.labelMedium, color = LightTextMuted)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neoBrutalism(backgroundColor = Pearl, shadowOffset = 8.dp)
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                Text(
                    text = "SELECT OPERATIONAL MODE",
                    style = GatherTypography.labelLarge,
                    color = Coal
                )

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    val roles = listOf(
                        UserRole.STUDENT to "STUDENT PARTICIPANT" to "DISCOVER & RSVP TO CAMPUS EVENTS",
                        UserRole.HOST to "EVENT ORGANIZER" to "CREATE & MANAGE EDITORIAL EDITIONS"
                    )

                    roles.forEach { (rolePair, desc) ->
                        val (role, title) = rolePair
                        val isSelected = state.role == role

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .neoBrutalism(
                                    backgroundColor = if (isSelected) Sand else Snow,
                                    shadowOffset = if (isSelected) 2.dp else 6.dp,
                                    borderWidth = 3.dp
                                )
                                .clickable { viewModel.setRole(role) }
                                .padding(20.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .neoBrutalism(backgroundColor = if (isSelected) Coal else LightGlass, shadowOffset = 0.dp)
                                        .padding(12.dp)
                                ) {
                                    Icon(
                                        imageVector = if (role == UserRole.STUDENT) Icons.Default.Search else Icons.Default.Add,
                                        contentDescription = null,
                                        tint = if (isSelected) Snow else Coal
                                    )
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = title,
                                        style = GatherTypography.titleLarge,
                                        color = Coal
                                    )
                                    Text(
                                        text = desc.uppercase(),
                                        style = GatherTypography.labelSmall,
                                        color = Slate
                                    )
                                }
                            }
                        }
                    }
                }

                BrutalistButton(
                    text = "INITIALIZE JOURNAL",
                    onClick = { viewModel.saveProfile(onFinish) },
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
    }
}