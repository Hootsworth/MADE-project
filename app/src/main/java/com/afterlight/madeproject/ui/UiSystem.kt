package com.afterlight.madeproject.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.afterlight.madeproject.domain.model.Event
import com.afterlight.madeproject.domain.model.VibeTag
import com.afterlight.madeproject.utils.DateTimeUtils
import com.afterlight.madeproject.ui.theme.*

/**
 * Higher-level composables that compose the brutalist component primitives.
 *
 * IMPORTANT: Basic primitives (BrutalistButton, BrutalistTextField) live
 * exclusively in [com.afterlight.madeproject.ui.components.BrutalistComponents.kt].
 * Do NOT duplicate them here.
 */

// ── Vibe Chips ──────────────────────────────────────────────────────────
@Composable
fun VibeChips(
    selected: List<String>,
    onToggle: (String) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(VibeTag.entries.toList()) { vibe ->
            val isSelected = selected.contains(vibe.name.lowercase())
            Box(
                modifier = Modifier
                    .neoBrutalism(
                        backgroundColor = if (isSelected) Coal else Snow,
                        shadowOffset = 2.dp,
                        borderWidth = 2.dp
                    )
                    .clickable { onToggle(vibe.name.lowercase()) }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = vibe.name.uppercase(),
                    style = GatherTypography.labelMedium,
                    color = if (isSelected) Snow else Coal
                )
            }
        }
    }
}

// ── Event Editorial Card ────────────────────────────────────────────────
@Composable
fun EventEditorialCard(
    event: Event,
    tickerText: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .neoBrutalism(backgroundColor = Pearl, shadowOffset = 8.dp)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = event.coverImageUrl.ifBlank { "https://source.unsplash.com/800x600/?event,campus" },
                    contentDescription = event.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(Coal),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .neoBrutalism(backgroundColor = Moss, shadowOffset = 2.dp)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = event.category.ifBlank { "Editorial pick" }.uppercase(),
                        style = GatherTypography.labelSmall,
                        color = Snow
                    )
                }
            }

            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = event.title.uppercase(),
                    style = GatherTypography.titleLarge,
                    color = Coal
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = event.hostName.ifBlank { "Host" }.uppercase(),
                            style = GatherTypography.labelLarge,
                            color = Coal
                        )
                        Text(
                            text = DateTimeUtils.formatEventTime(event.dateTime),
                            style = GatherTypography.labelMedium,
                            color = Copper
                        )
                    }
                    Box(
                        modifier = Modifier
                            .neoBrutalism(backgroundColor = Sand, shadowOffset = 2.dp)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "${event.spotsLeft} SPOTS",
                            style = GatherTypography.labelSmall,
                            color = Coal
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neoBrutalism(backgroundColor = Snow, shadowOffset = 2.dp)
                        .padding(12.dp)
                ) {
                    Text(text = tickerText, style = GatherTypography.labelMedium, color = Coal)
                }
            }
        }
    }
}

// ── Shimmer Card ────────────────────────────────────────────────────────
@Composable
fun ShimmerCard(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .neoBrutalism(backgroundColor = LightGlass, shadowOffset = 4.dp)
    )
}

// ── Department Leaderboard ──────────────────────────────────────────────
@Composable
fun DepartmentLeaderboardBlock(rows: List<Pair<String, Int>>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neoBrutalism(backgroundColor = Pearl, shadowOffset = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "DEPARTMENT RANKING".uppercase(),
                style = GatherTypography.labelLarge,
                color = Coal
            )
            rows.forEachIndexed { index, row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neoBrutalism(
                            backgroundColor = if (index == 0) Sand else Snow,
                            shadowOffset = 2.dp
                        )
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${index + 1}. ${row.first}".uppercase(),
                        style = GatherTypography.titleLarge,
                        color = Coal
                    )
                    Text(
                        text = row.second.toString(),
                        style = GatherTypography.titleLarge,
                        color = Moss
                    )
                }
            }
        }
    }
}