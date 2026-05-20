package com.afterlight.madeproject.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.afterlight.madeproject.domain.model.Event
import com.afterlight.madeproject.domain.model.VibeTag
import com.afterlight.madeproject.utils.DateTimeUtils
import com.afterlight.madeproject.ui.theme.*

// ── Vibe Chips ──────────────────────────────────────────────────────────
@Composable
fun VibeChips(
    selected: List<String>,
    onToggle: (String) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(VibeTag.entries.toList()) { vibe ->
            val isSelected = selected.contains(vibe.name.lowercase())
            Surface(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onToggle(vibe.name.lowercase()) },
                shape = CircleShape,
                color = if (isSelected) Coal else Pearl.copy(alpha = 0.4f)
            ) {
                Text(
                    text = vibe.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = GatherTypography.labelMedium,
                    color = if (isSelected) Snow else Coal,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
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
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Pearl),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
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
                Surface(
                    shape = CircleShape,
                    color = Moss.copy(alpha = 0.9f),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = event.category.ifBlank { "Editorial pick" },
                        style = GatherTypography.labelSmall,
                        color = Snow,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = event.title,
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
                            text = event.hostName.ifBlank { "Host" },
                            style = GatherTypography.labelLarge,
                            color = Coal
                        )
                        Text(
                            text = DateTimeUtils.formatEventTime(event.dateTime),
                            style = GatherTypography.labelMedium,
                            color = Copper
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = Sand.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "${event.spotsLeft} Spots",
                            style = GatherTypography.labelSmall,
                            color = Coal,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Pearl.copy(alpha = 0.3f)
                ) {
                    Text(
                        text = tickerText,
                        style = GatherTypography.labelMedium,
                        color = Coal,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

// ── Shimmer Card ────────────────────────────────────────────────────────
@Composable
fun ShimmerCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(24.dp),
        color = Pearl.copy(alpha = 0.3f)
    ) {}
}

// ── Department Leaderboard ──────────────────────────────────────────────
@Composable
fun DepartmentLeaderboardBlock(rows: List<Pair<String, Int>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Pearl),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            rows.forEachIndexed { index, row ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = if (index == 0) Sand.copy(alpha = 0.2f) else Pearl.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${index + 1}. ${row.first}",
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
}