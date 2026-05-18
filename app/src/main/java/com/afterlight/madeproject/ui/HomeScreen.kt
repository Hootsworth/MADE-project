package com.afterlight.madeproject.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.afterlight.madeproject.domain.model.Event
import com.afterlight.madeproject.utils.DateTimeUtils
import com.afterlight.madeproject.ui.theme.*

@Composable
fun HomeScreen(
    onEventClick: (String) -> Unit,
    onHostClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val feed by viewModel.feed.collectAsStateWithLifecycle()
    val week by viewModel.week.collectAsStateWithLifecycle()
    val aiBrief by viewModel.aiBrief.collectAsStateWithLifecycle()
    val dailyQuote by viewModel.dailyQuote.collectAsStateWithLifecycle()

    val leadEvent = feed.firstOrNull() ?: week.firstOrNull()
    // DiceBear avatar URL (API #1) – deterministic per session
    val avatarUrl = viewModel.externalApiService.diceBearAvatarUrl("campus-user-home")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Snow)
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                // Header Row — FIX: dynamic greeting based on time of day
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // DiceBear avatar (API endpoint #1)
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "User avatar",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .border(2.dp, Coal, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Column {
                            Text(
                                text = viewModel.greeting(),
                                style = GatherTypography.labelMedium,
                                color = LightTextMuted
                            )
                            Text(
                                text = "Campus Creator.",
                                style = GatherTypography.displayLarge,
                                color = Coal
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TopActionBubble(icon = Icons.Default.NotificationsNone)
                        TopActionBubble(icon = Icons.Default.Bolt)
                    }
                }

                // AI Brief Block (uses OpenAI/Gemini API #3/#4 for feed summary)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neoBrutalism(backgroundColor = Pearl, shadowOffset = 4.dp)
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "AI BRIEF //", style = GatherTypography.labelLarge, color = Coal)
                        Text(text = aiBrief, style = GatherTypography.bodyLarge, color = LightTextMuted)
                    }
                }

                // Daily Quote from Quotable API (#6) — shown as a subtle block
                dailyQuote?.let { (content, author) ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .neoBrutalism(backgroundColor = Sand, shadowOffset = 2.dp)
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "DAILY INSPIRATION //",
                                style = GatherTypography.labelSmall,
                                color = Coal
                            )
                            Text(
                                text = "\"$content\"",
                                style = GatherTypography.bodyLarge,
                                color = Coal
                            )
                            Text(
                                text = "— $author",
                                style = GatherTypography.labelMedium,
                                color = Moss
                            )
                        }
                    }
                }

                // Hero Event Card (Lead Event)
                leadEvent?.let { event ->
                    val coverUrl = event.coverImageUrl.ifBlank {
                        viewModel.externalApiService.unsplashCoverUrl(event.category.ifBlank { "event,campus" })
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onEventClick(event.eventId) }
                    ) {
                        AsyncImage(
                            model = coverUrl,
                            contentDescription = event.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Coal.copy(alpha = 0.9f)),
                                        startY = 200f
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "FEATURED EVENT",
                                style = GatherTypography.labelMedium,
                                color = Snow,
                                modifier = Modifier
                                    .background(Copper)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                            Text(
                                text = event.title,
                                style = GatherTypography.displayLarge,
                                color = Snow,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Dual Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .neoBrutalism(backgroundColor = Moss, shadowOffset = 4.dp)
                            .clickable(onClick = onHostClick)
                            .padding(20.dp)
                    ) {
                        Column {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Snow, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("Host Event", style = GatherTypography.titleLarge, color = Snow)
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .neoBrutalism(backgroundColor = Sand, shadowOffset = 4.dp)
                            .clickable { /* Future Feature: Discover/Scan */ }
                            .padding(20.dp)
                    ) {
                        Column {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Coal, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("Discover", style = GatherTypography.titleLarge, color = Coal)
                        }
                    }
                }
            }
        }

        item {
            Text(text = "THE LOCAL REGISTRY", style = GatherTypography.labelLarge, color = Coal)
        }

        if (week.isNotEmpty() || feed.isNotEmpty()) {
            item {
                val carousel = if (week.isNotEmpty()) week else feed
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(carousel) { event ->
                        Box(
                            modifier = Modifier
                                .width(240.dp)
                                .neoBrutalism(backgroundColor = Pearl, shadowOffset = 6.dp)
                                .clickable { onEventClick(event.eventId) }
                        ) {
                            Column {
                                // Unsplash fallback for events without cover images (API #2)
                                val coverUrl = event.coverImageUrl.ifBlank {
                                    viewModel.externalApiService.unsplashCoverUrl(
                                        event.category.ifBlank { "event,campus" }
                                    )
                                }
                                AsyncImage(
                                    model = coverUrl,
                                    contentDescription = event.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .background(Coal),
                                    contentScale = ContentScale.Crop
                                )
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = event.title,
                                        style = GatherTypography.titleLarge,
                                        color = Coal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = DateTimeUtils.formatEventTime(event.dateTime),
                                        style = GatherTypography.labelMedium,
                                        color = Copper
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopActionBubble(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .neoBrutalism(backgroundColor = Snow, shadowOffset = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Coal, modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun BrutalistFeatureCard(
    badge: String,
    title: String,
    tint: Color,
    textColor: Color = Coal,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neoBrutalism(backgroundColor = tint, shadowOffset = 8.dp)
            .clickable(onClick = onClick)
            .padding(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = badge,
                style = GatherTypography.labelMedium,
                color = textColor,
                modifier = Modifier
                    .border(2.dp, textColor)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
            Text(text = title, style = GatherTypography.headlineLarge, color = textColor)
        }
    }
}