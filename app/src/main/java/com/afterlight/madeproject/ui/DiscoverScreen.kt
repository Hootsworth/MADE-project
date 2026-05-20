package com.afterlight.madeproject.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items as lazyRowItems
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp // <-- The crucial import for text scaling
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.afterlight.madeproject.domain.model.Event
import com.afterlight.madeproject.ui.components.SmoothTextField
import com.afterlight.madeproject.ui.theme.*
import com.afterlight.madeproject.utils.DateTimeUtils

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@Composable
fun DiscoverScreen(
    onEventClick: (String) -> Unit,
    viewModel: DiscoverViewModel = hiltViewModel()
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val category by viewModel.category.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val leaderboard by viewModel.leaderboard.collectAsStateWithLifecycle()

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .background(Snow)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 120.dp, top = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalItemSpacing = 16.dp
    ) {
        item(span = StaggeredGridItemSpan.FullLine) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Pearl),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "ATLAS",
                            style = GatherTypography.displayLarge.copy(letterSpacing = 4.sp),
                            color = Coal
                        )
                        Text(
                            text = "Global registry for campus editions",
                            style = GatherTypography.bodyLarge,
                            color = LightTextMuted
                        )
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = Snow
                    ) {
                        SmoothTextField(
                            value = query,
                            onValueChange = viewModel::updateQuery,
                            label = "Search events or locations",
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val sectors = listOf("Workshop" to "Workshops", "Music" to "Audio", "Tech" to "Tech")
                        lazyRowItems(sectors) { (cat, label) ->
                            val selected = category == cat
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (selected) Coal else Snow,
                                border = CardDefaults.outlinedCardBorder(),
                                modifier = Modifier.clickable { viewModel.setCategory(if (selected) null else cat) }
                            ) {
                                Text(
                                    text = label,
                                    style = GatherTypography.labelMedium,
                                    color = if (selected) Snow else Coal,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (leaderboard.isNotEmpty()) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Top Navigators",
                        style = GatherTypography.titleLarge,
                        color = LightTextMuted
                    )
                    DepartmentLeaderboardBlock(
                        rows = leaderboard.map { it.name to it.monthlyScore }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        if (searchResults.isEmpty()) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Pearl),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    )
                    {
                        Text(
                            text = "No waypoints found. Try a different sector or search term.",
                            style = GatherTypography.bodyLarge,
                            color = LightTextMuted
                        )
                    }
                }
            }
        }

        items(searchResults, key = { it.eventId }) { event ->
            AtlasWaypointCard(event = event, onEventClick = onEventClick)
        }
    }
}

@Composable
private fun AtlasWaypointCard(event: Event, onEventClick: (String) -> Unit) {
    // Generate a pseudo-random height based on the event ID hash to create the masonry effect
    val cardHeight = remember(event.eventId) { (220..320).random().dp }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight)
            .clickable { onEventClick(event.eventId) },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Coal),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Image
            if (event.coverImageUrl.isNotBlank()) {
                AsyncImage(
                    model = event.coverImageUrl,
                    contentDescription = event.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Gradient Overlay for text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f)),
                            startY = 120f
                        )
                    )
            )

            // Content anchored to the bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Category Tag
                Surface(
                    color = Copper,
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = event.category.ifBlank { "Uncharted" }.uppercase(),
                        style = GatherTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Snow,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                Text(
                    text = event.title,
                    style = GatherTypography.titleMedium,
                    color = Snow,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = DateTimeUtils.formatEventTime(event.dateTime),
                        style = GatherTypography.labelSmall,
                        color = Sand
                    )
                    Text(
                        text = "• ${event.spotsLeft} spots",
                        style = GatherTypography.labelSmall,
                        color = Snow.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}