package com.afterlight.madeproject.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.afterlight.madeproject.domain.model.UserRole
import com.afterlight.madeproject.ui.components.SmoothButton
import com.afterlight.madeproject.ui.theme.*
import com.afterlight.madeproject.utils.DateTimeUtils

@Composable
fun MyEventsScreen(
    onEventClick: (String) -> Unit,
    onHostControlsClick: (String) -> Unit,
    viewModel: MyEventsViewModel = hiltViewModel()
) {
    val upcoming by viewModel.upcomingEvents.collectAsStateWithLifecycle()
    val past by viewModel.pastEvents.collectAsStateWithLifecycle()
    val hosted by viewModel.hostedEvents.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val canHost = profile?.role == UserRole.HOST
    var selectedTab by remember { mutableStateOf(0) }

    val tabs = if (canHost) listOf("Upcoming", "Past", "Hosted") else listOf("Upcoming", "Past")
    val list = when {
        !canHost -> if (selectedTab == 0) upcoming else past
        selectedTab == 0 -> upcoming
        selectedTab == 1 -> past
        else -> hosted
    }

    LaunchedEffect(canHost) {
        if (!canHost && selectedTab > 1) selectedTab = 0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Snow)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp) // Matched spacing with Settings
    ) {
        Column(
            modifier = Modifier.padding(top = 40.dp), // Matched top padding
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(text = "Ticket Wallet", style = GatherTypography.displayLarge, color = Coal)

            // Segmented Control (Mirrors ThemePicker)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Pearl.copy(alpha = 0.4f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = index == selectedTab
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .clickable { selectedTab = index },
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) Coal else Color.Transparent,
                        shadowElevation = if (isSelected) 2.dp else 0.dp
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                style = GatherTypography.labelMedium,
                                color = if (isSelected) Snow else Coal.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (list.isEmpty()) {
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
                                text = "Wallet is empty.",
                                style = GatherTypography.bodyLarge,
                                color = LightTextMuted
                            )
                        }
                    }
                }
            }
            items(list) { event ->
                // Flat, Structured Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEventClick(event.eventId) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Snow),
                    border = BorderStroke(1.dp, Coal.copy(alpha = 0.12f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.padding(24.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = event.title,
                                    style = GatherTypography.titleLarge,
                                    color = Coal
                                )
                                Text(
                                    text = DateTimeUtils.formatEventTime(event.dateTime),
                                    style = GatherTypography.bodyMedium,
                                    color = LightTextMuted
                                )
                                if (selectedTab == 0) {
                                    // Sharper Countdown Tag
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Moss.copy(alpha = 0.1f),
                                        border = BorderStroke(1.dp, Moss.copy(alpha = 0.2f)),
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Text(
                                            text = "Starts in ${DateTimeUtils.countdown(event.dateTime)}",
                                            style = GatherTypography.labelSmall,
                                            color = Moss,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Ticket Stub QR Code Section
                        if (selectedTab == 0) {
                            HorizontalDivider(color = Coal.copy(alpha = 0.12f), thickness = 1.dp)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Pearl.copy(alpha = 0.3f)) // Subtle distinct footer background
                                    .padding(24.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Admit One", style = GatherTypography.labelLarge, color = Coal)
                                    Text("ID: ${event.eventId.take(8).uppercase()}", style = GatherTypography.bodySmall, color = LightTextMuted)
                                }
                                val qrUrl = remember(event.eventId) {
                                    viewModel.externalApiService.qrCodeUrl(
                                        "https://paperlike.app/events/${event.eventId}"
                                    )
                                }
                                // Geometric QR Container
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Snow,
                                    border = BorderStroke(1.dp, Coal.copy(alpha = 0.12f))
                                ) {
                                    AsyncImage(
                                        model = qrUrl,
                                        contentDescription = "QR code for ${event.title}",
                                        modifier = Modifier
                                            .size(80.dp)
                                            .padding(8.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }

                        // Host Controls Section
                        if (canHost && selectedTab == 2) {
                            HorizontalDivider(color = Coal.copy(alpha = 0.12f), thickness = 1.dp)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Pearl.copy(alpha = 0.3f))
                                    .padding(16.dp)
                            ) {
                                SmoothButton(
                                    text = "Host Controls",
                                    onClick = { onHostControlsClick(event.eventId) },
                                    modifier = Modifier.fillMaxWidth(),
                                    containerColor = Coal,
                                    contentColor = Snow
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}