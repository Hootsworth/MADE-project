package com.afterlight.madeproject.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.afterlight.madeproject.domain.model.Event
import com.afterlight.madeproject.ui.components.BrutalistTextField
import com.afterlight.madeproject.ui.theme.*
import com.afterlight.madeproject.utils.DateTimeUtils

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@Composable
fun DiscoverScreen(
    onEventClick: (String) -> Unit,
    viewModel: DiscoverViewModel = hiltViewModel()
) {
    // FIX: Now using DiscoverViewModel instead of local state
    val query by viewModel.query.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val leaderboard by viewModel.leaderboard.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Snow)
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(bottom = 100.dp, top = 32.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "ATLAS DISCOVER.",
                    style = GatherTypography.displayLarge,
                    color = Coal
                )

                // Brutalist Search Block
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neoBrutalism(backgroundColor = Pearl, shadowOffset = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        BrutalistTextField(
                            value = query,
                            onValueChange = viewModel::updateQuery,
                            label = "SEARCH REGISTRY"
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("Workshop" to "WORKSHOPS", "Music" to "MUSIC", "Tech" to "TECH").forEach { (cat, label) ->
                                Box(
                                    modifier = Modifier
                                        .neoBrutalism(backgroundColor = Snow, shadowOffset = 2.dp)
                                        .clickable { viewModel.setCategory(cat) }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = label,
                                        style = GatherTypography.labelMedium,
                                        color = Coal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Leaderboard Section
        if (leaderboard.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    HorizontalDivider(thickness = 4.dp, color = Coal)
                    Text(
                        text = "DEPARTMENT RANKING //",
                        style = GatherTypography.labelLarge,
                        color = Coal
                    )
                    DepartmentLeaderboardBlock(
                        rows = leaderboard.map { it.name to it.monthlyScore }
                    )
                }
            }
        }

        // Search Results — FIX: now rendering actual results from the VM
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                HorizontalDivider(thickness = 4.dp, color = Coal)
                Text(
                    text = "RESULTS // ${searchResults.size} FOUND",
                    style = GatherTypography.labelLarge,
                    color = Coal
                )
            }
        }

        if (searchResults.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neoBrutalism(backgroundColor = Pearl, shadowOffset = 6.dp)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NO MATCHING EVENTS FOUND.",
                        style = GatherTypography.labelLarge,
                        color = LightTextMuted
                    )
                }
            }
        }

        items(searchResults) { event ->
            DiscoverEventCard(event = event, onEventClick = onEventClick)
        }
    }
}

@Composable
private fun DiscoverEventCard(event: Event, onEventClick: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neoBrutalism(backgroundColor = Pearl, shadowOffset = 6.dp)
            .clickable { onEventClick(event.eventId) }
    ) {
        Column {
            if (event.coverImageUrl.isNotBlank()) {
                AsyncImage(
                    model = event.coverImageUrl,
                    contentDescription = event.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Coal),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = event.title.uppercase(),
                    style = GatherTypography.titleLarge,
                    color = Coal,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${event.hostName.ifBlank { "Host" }.uppercase()} · ${event.category.ifBlank { "General" }.uppercase()}",
                    style = GatherTypography.bodyMedium,
                    color = Slate
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = DateTimeUtils.formatEventTime(event.dateTime),
                        style = GatherTypography.labelMedium,
                        color = Copper
                    )
                    Box(
                        modifier = Modifier
                            .neoBrutalism(backgroundColor = Sand, shadowOffset = 2.dp)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${event.spotsLeft} SPOTS",
                            style = GatherTypography.labelSmall,
                            color = Coal
                        )
                    }
                }
            }
        }
    }
}