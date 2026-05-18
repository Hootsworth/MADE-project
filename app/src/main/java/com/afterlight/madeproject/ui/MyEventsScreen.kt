package com.afterlight.madeproject.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.afterlight.madeproject.utils.DateTimeUtils
import com.afterlight.madeproject.ui.components.BrutalistButton
import com.afterlight.madeproject.ui.theme.*

@Composable
fun MyEventsScreen(
    onEventClick: (String) -> Unit,
    onHostControlsClick: (String) -> Unit,
    viewModel: MyEventsViewModel = hiltViewModel()
) {
    val upcoming by viewModel.upcomingEvents.collectAsStateWithLifecycle()
    val past by viewModel.pastEvents.collectAsStateWithLifecycle()
    val hosted by viewModel.hostedEvents.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) }

    val tabs = listOf("UPCOMING", "PAST", "HOSTED")
    val list = when (selectedTab) {
        0 -> upcoming
        1 -> past
        else -> hosted
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Snow)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Column(
            modifier = Modifier.padding(top = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "TICKET WALLET.", style = GatherTypography.displayLarge, color = Coal)

            // Brutalist Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = index == selectedTab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .neoBrutalism(
                                backgroundColor = if (isSelected) Copper else Snow,
                                shadowOffset = if (isSelected) 4.dp else 2.dp,
                                borderWidth = 2.dp
                            )
                            .clickable { selectedTab = index }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            style = GatherTypography.labelMedium,
                            color = if (isSelected) Snow else Coal
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (list.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .neoBrutalism(backgroundColor = Pearl, shadowOffset = 6.dp)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "WALLET IS EMPTY.",
                            style = GatherTypography.labelLarge,
                            color = LightTextMuted
                        )
                    }
                }
            }
            items(list) { event ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neoBrutalism(backgroundColor = Pearl, shadowOffset = 6.dp)
                        .clickable { onEventClick(event.eventId) }
                ) {
                    Column {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = event.title.uppercase(),
                                    style = GatherTypography.titleLarge,
                                    color = Coal
                                )
                                Text(
                                    text = DateTimeUtils.formatEventTime(event.dateTime),
                                    style = GatherTypography.labelMedium,
                                    color = Moss
                                )
                                if (selectedTab == 0) {
                                    Box(
                                        modifier = Modifier
                                            .neoBrutalism(backgroundColor = Sand, shadowOffset = 2.dp)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "STARTS IN ${DateTimeUtils.countdown(event.dateTime).uppercase()}",
                                            style = GatherTypography.labelSmall,
                                            color = Coal
                                        )
                                    }
                                }
                            }
                        }

                        // Ticket Stub QR Code Section
                        if (selectedTab == 0) {
                            androidx.compose.material3.HorizontalDivider(color = Coal.copy(alpha = 0.3f), thickness = 1.dp)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Snow)
                                    .padding(20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("ADMIT ONE", style = GatherTypography.labelLarge, color = Coal)
                                    Text("ID: ${event.eventId.take(8).uppercase()}", style = GatherTypography.labelSmall, color = LightTextMuted)
                                }
                                val qrUrl = remember(event.eventId) {
                                    viewModel.externalApiService.qrCodeUrl(
                                        "https://paperlike.app/events/${event.eventId}"
                                    )
                                }
                                Box(modifier = Modifier.neoBrutalism(borderWidth = 2.dp, shadowOffset = 2.dp)) {
                                    AsyncImage(
                                        model = qrUrl,
                                        contentDescription = "QR code for ${event.title}",
                                        modifier = Modifier.size(80.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }

                        if (selectedTab == 2) {
                            androidx.compose.material3.HorizontalDivider(color = Coal.copy(alpha = 0.3f), thickness = 1.dp)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Snow)
                                    .padding(16.dp)
                            ) {
                                BrutalistButton(
                                    text = "HOST CONTROLS",
                                    onClick = { onHostControlsClick(event.eventId) },
                                    modifier = Modifier.fillMaxWidth(),
                                    backgroundColor = Coal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}