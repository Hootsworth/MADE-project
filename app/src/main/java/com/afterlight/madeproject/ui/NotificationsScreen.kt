package com.afterlight.madeproject.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.afterlight.madeproject.ui.theme.*

@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Snow)
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(bottom = 120.dp, top = 40.dp), // Matched top padding
        verticalArrangement = Arrangement.spacedBy(32.dp) // Matched spacing
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text(text = "Signal Feed", style = GatherTypography.displayLarge, color = Coal)

                // Geometric Info Banner
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Sand.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Sand.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "System log: Only relevant reminders and updates are archived here.",
                        style = GatherTypography.bodyMedium,
                        color = Coal.copy(alpha = 0.8f),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        if (notifications.isEmpty()) {
            item {
                // Geometric Empty State
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Snow,
                    border = BorderStroke(1.dp, Coal.copy(alpha = 0.12f))
                ) {
                    Box(
                        modifier = Modifier.padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Initializing signal feed...",
                            style = GatherTypography.bodyLarge,
                            color = LightTextMuted
                        )
                    }
                }
            }
        } else {
            items(notifications) { note ->
                // Flat, Structured Notification Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Snow),
                    border = BorderStroke(1.dp, Coal.copy(alpha = 0.12f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp), // Sharper icon background
                            color = Pearl.copy(alpha = 0.6f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = Coal,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        val formattedNote = note.lowercase().replaceFirstChar { it.uppercase() }
                        Text(
                            text = formattedNote,
                            style = GatherTypography.bodyLarge,
                            color = Coal
                        )
                    }
                }
            }
        }
    }
}