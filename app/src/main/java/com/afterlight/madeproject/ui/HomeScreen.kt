package com.afterlight.madeproject.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.afterlight.madeproject.domain.model.Event
import com.afterlight.madeproject.domain.model.EventStatus
import com.afterlight.madeproject.domain.model.UserRole
import com.afterlight.madeproject.ui.theme.*
import com.afterlight.madeproject.utils.DateTimeUtils

@Composable
fun HomeScreen(
    onEventClick: (String) -> Unit,
    onHostClick: () -> Unit,
    onScanClick: () -> Unit,
    onPosterScanClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val feed by viewModel.feed.collectAsStateWithLifecycle()
    val week by viewModel.week.collectAsStateWithLifecycle()
    val aiBrief by viewModel.aiBrief.collectAsStateWithLifecycle()
    val dailyQuote by viewModel.dailyQuote.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val isHost = profile?.role == UserRole.HOST

    if (isHost) {
        HostHomeContent(
            feed = feed,
            week = week,
            aiBrief = aiBrief,
            dailyQuote = dailyQuote,
            onEventClick = onEventClick,
            onHostClick = onHostClick,
            onScanClick = onScanClick,
            onPosterScanClick = onPosterScanClick,
            viewModel = viewModel
        )
    } else {
        StudentHomeContent(
            feed = feed,
            week = week,
            dailyQuote = dailyQuote,
            onEventClick = onEventClick,
            onScanClick = onScanClick,
            onPosterScanClick = onPosterScanClick,
            viewModel = viewModel
        )
    }
}

@Composable
private fun HostHomeContent(
    feed: List<Event>,
    week: List<Event>,
    aiBrief: String,
    dailyQuote: Pair<String, String>?,
    onEventClick: (String) -> Unit,
    onHostClick: () -> Unit,
    onScanClick: () -> Unit,
    onPosterScanClick: () -> Unit,
    viewModel: HomeViewModel
) {
    val now = System.currentTimeMillis()
    val upcomingFeed = feed.filterNot { it.isOver(now) }
    val upcomingWeek = week.filterNot { it.isOver(now) }
    val leadEvent = upcomingFeed.firstOrNull() ?: upcomingWeek.firstOrNull()
    val avatarUrl = viewModel.externalApiService.diceBearAvatarUrl("campus-user-home")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Snow)
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = 40.dp, bottom = 120.dp), // Matched top padding
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Coal.copy(alpha = 0.12f))
                        ) {
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = "User avatar",
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(Pearl.copy(alpha = 0.3f)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(text = viewModel.greeting(), style = GatherTypography.labelMedium, color = LightTextMuted)
                            Text(text = "Campus Creator.", style = GatherTypography.displayLarge, color = Coal)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TopActionBubble(icon = Icons.Default.NotificationsNone)
                        TopActionBubble(icon = Icons.Default.Bolt)
                    }
                }

                // Geometric AI Brief Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Snow),
                    border = BorderStroke(1.dp, Coal.copy(alpha = 0.12f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Copper, modifier = Modifier.size(16.dp))
                            Text(text = "AI Brief", style = GatherTypography.labelMedium, color = Coal)
                        }
                        Text(text = aiBrief, style = GatherTypography.bodyLarge, color = LightTextMuted)
                    }
                }

                dailyQuote?.let { (content, author) ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Sand.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Sand.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "Daily Inspiration", style = GatherTypography.labelSmall, color = Moss)
                            Text(text = "\"$content\"", style = GatherTypography.bodyLarge, color = Coal)
                            Text(text = "— $author", style = GatherTypography.labelMedium, color = Coal.copy(alpha = 0.6f))
                        }
                    }
                }

                leadEvent?.let { event ->
                    val coverUrl = event.coverImageUrl.ifBlank {
                        viewModel.externalApiService.unsplashCoverUrl(event.category.ifBlank { "event,campus" })
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                            .clickable { onEventClick(event.eventId) },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Coal.copy(alpha = 0.15f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
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
                                            colors = listOf(Color.Transparent, Snow.copy(alpha = 0.95f)),
                                            startY = 300f
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val urgency = urgencyMessage(event, now)

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Snow.copy(alpha = 0.9f),
                                    border = BorderStroke(1.dp, Coal.copy(alpha = 0.1f))
                                ) {
                                    Text(
                                        text = "Featured Edition",
                                        style = GatherTypography.labelSmall,
                                        color = Coal,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                                Text(
                                    text = event.title,
                                    style = GatherTypography.displayLarge,
                                    color = Coal,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                urgency?.let {
                                    Surface(shape = RoundedCornerShape(6.dp), color = Copper) {
                                        Text(
                                            text = it,
                                            style = GatherTypography.labelSmall,
                                            color = Snow,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Squared off action row
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onHostClick),
                        color = Moss,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Snow, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Host", style = GatherTypography.labelLarge, color = Snow)
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onScanClick),
                        color = Snow,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Coal.copy(alpha = 0.12f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Coal, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Scan QR", style = GatherTypography.labelLarge, color = Coal)
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onPosterScanClick),
                        color = Snow,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Coal.copy(alpha = 0.12f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Copper, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Poster", style = GatherTypography.labelLarge, color = Coal) // Shortened for fit
                        }
                    }
                }
            }
        }

        item {
            Text(text = "The Local Registry", style = GatherTypography.titleLarge, color = Coal)
        }

        if (upcomingWeek.isNotEmpty() || upcomingFeed.isNotEmpty()) {
            item {
                val carousel = if (upcomingWeek.isNotEmpty()) upcomingWeek else upcomingFeed
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(end = 24.dp)
                ) {
                    items(carousel) { event ->
                        Card(
                            modifier = Modifier.width(260.dp).clickable { onEventClick(event.eventId) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Snow),
                            border = BorderStroke(1.dp, Coal.copy(alpha = 0.12f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column {
                                val coverUrl = event.coverImageUrl.ifBlank {
                                    viewModel.externalApiService.unsplashCoverUrl(event.category.ifBlank { "event,campus" })
                                }
                                val urgency = urgencyMessage(event, now)

                                AsyncImage(
                                    model = coverUrl,
                                    contentDescription = event.title,
                                    modifier = Modifier.fillMaxWidth().height(140.dp),
                                    contentScale = ContentScale.Crop
                                )
                                Column(
                                    modifier = Modifier.padding(20.dp),
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
                                        style = GatherTypography.bodyMedium,
                                        color = LightTextMuted
                                    )
                                    urgency?.let {
                                        Text(text = it, style = GatherTypography.labelSmall, color = Copper)
                                    }
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
private fun StudentHomeContent(
    feed: List<Event>,
    week: List<Event>,
    dailyQuote: Pair<String, String>?,
    onEventClick: (String) -> Unit,
    onScanClick: () -> Unit,
    onPosterScanClick: () -> Unit,
    viewModel: HomeViewModel
) {
    val now = System.currentTimeMillis()
    val upcomingFeed = feed.filterNot { it.isOver(now) }
    val upcomingWeek = week.filterNot { it.isOver(now) }
    val spotlight = (upcomingWeek.take(3) + upcomingFeed.take(3)).distinctBy { it.eventId }.take(5)
    val nextEvent = spotlight.firstOrNull()
    val avatarUrl = viewModel.externalApiService.diceBearAvatarUrl("campus-student-home")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Snow)
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = 40.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Coal.copy(alpha = 0.12f))
                        ) {
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = "Student avatar",
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(Pearl.copy(alpha = 0.3f)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(text = viewModel.greeting(), style = GatherTypography.labelMedium, color = LightTextMuted)
                            Text(text = "Student Hub.", style = GatherTypography.displayLarge, color = Coal)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Sand.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Sand.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "For Students",
                            style = GatherTypography.labelSmall,
                            color = Coal,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Snow,
                    border = BorderStroke(1.dp, Coal.copy(alpha = 0.12f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "Your campus feed", style = GatherTypography.labelMedium, color = Moss)
                        Text(
                            text = "Discover, RSVP, and keep your ticket wallet in one place.",
                            style = GatherTypography.bodyLarge,
                            color = Coal
                        )
                    }
                }

                dailyQuote?.let { (content, author) ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Sand.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Sand.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "Campus Quote", style = GatherTypography.labelSmall, color = Moss)
                            Text(text = "\"$content\"", style = GatherTypography.bodyLarge, color = Coal)
                            Text(text = "— $author", style = GatherTypography.labelMedium, color = Coal.copy(alpha = 0.6f))
                        }
                    }
                }

                nextEvent?.let { event ->
                    val coverUrl = event.coverImageUrl.ifBlank {
                        viewModel.externalApiService.unsplashCoverUrl(event.category.ifBlank { "campus,event" })
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clickable { onEventClick(event.eventId) },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Coal.copy(alpha = 0.15f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
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
                                            colors = listOf(Color.Transparent, Snow.copy(alpha = 0.92f)),
                                            startY = 280f
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(22.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Snow,
                                    border = BorderStroke(1.dp, Copper.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = "Next up",
                                        style = GatherTypography.labelSmall,
                                        color = Copper,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Text(
                                    text = event.title,
                                    style = GatherTypography.displayLarge,
                                    color = Coal,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = DateTimeUtils.formatEventTime(event.dateTime),
                                    style = GatherTypography.bodyMedium,
                                    color = LightTextMuted
                                )
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onScanClick),
                        color = Snow,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Coal.copy(alpha = 0.12f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Coal, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Scan QR", style = GatherTypography.labelLarge, color = Coal)
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onPosterScanClick),
                        color = Snow,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Coal.copy(alpha = 0.12f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Copper, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Poster AI", style = GatherTypography.labelLarge, color = Coal)
                        }
                    }
                }
            }
        }

        item {
            Text(text = "Recommended for You", style = GatherTypography.titleLarge, color = Coal)
        }

        if (spotlight.isNotEmpty()) {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(end = 24.dp)
                ) {
                    items(spotlight) { event ->
                        Card(
                            modifier = Modifier
                                .width(220.dp)
                                .clickable { onEventClick(event.eventId) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Snow),
                            border = BorderStroke(1.dp, Coal.copy(alpha = 0.12f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column {
                                val coverUrl = event.coverImageUrl.ifBlank {
                                    viewModel.externalApiService.unsplashCoverUrl(event.category.ifBlank { "campus,event" })
                                }
                                AsyncImage(
                                    model = coverUrl,
                                    contentDescription = event.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(132.dp),
                                    contentScale = ContentScale.Crop
                                )
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
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
                                        style = GatherTypography.bodyMedium,
                                        color = LightTextMuted
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

private fun Event.isOver(now: Long = System.currentTimeMillis()): Boolean {
    return status == EventStatus.PAST || dateTime <= now
}

private fun urgencyMessage(event: Event, now: Long = System.currentTimeMillis()): String? {
    val minutesLeft = ((event.dateTime - now) / 60000L).coerceAtLeast(0L)
    val spotsLeft = event.spotsLeft

    return when {
        minutesLeft in 1..30 -> "Closing soon: ${minutesLeft}m left"
        spotsLeft in 1..5 -> "Limited: Only $spotsLeft left"
        spotsLeft in 6..12 -> "Filling fast: $spotsLeft left"
        else -> null
    }
}

@Composable
private fun TopActionBubble(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        modifier = Modifier.size(48.dp),
        shape = RoundedCornerShape(8.dp), // Flattened shape
        color = Pearl.copy(alpha = 0.6f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = null, tint = Coal, modifier = Modifier.size(22.dp))
        }
    }
}