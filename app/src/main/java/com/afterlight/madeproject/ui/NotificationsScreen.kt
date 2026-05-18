package com.afterlight.madeproject.ui

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
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
        contentPadding = PaddingValues(bottom = 100.dp, top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(text = "SIGNAL FEED.", style = GatherTypography.displayLarge, color = Coal)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neoBrutalism(backgroundColor = Sand, shadowOffset = 6.dp)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "SYSTEM LOG: ONLY RELEVANT REMINDERS AND UPDATES ARE ARCHIVED HERE.",
                        style = GatherTypography.labelMedium,
                        color = Coal
                    )
                }
            }
        }

        if (notifications.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neoBrutalism(backgroundColor = Pearl, shadowOffset = 4.dp)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "INITIALIZING SIGNAL FEED...",
                        style = GatherTypography.labelMedium,
                        color = LightTextMuted
                    )
                }
            }
        } else {
            items(notifications) { note ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neoBrutalism(backgroundColor = Pearl, shadowOffset = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .neoBrutalism(backgroundColor = Coal, shadowOffset = 0.dp)
                                .padding(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = null,
                                tint = Snow,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = note,
                            style = GatherTypography.bodyLarge,
                            color = Coal
                        )
                    }
                }
            }
        }
    }
}