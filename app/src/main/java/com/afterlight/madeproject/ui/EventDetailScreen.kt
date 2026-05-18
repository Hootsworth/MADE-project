package com.afterlight.madeproject.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.afterlight.madeproject.ui.components.BrutalistButton
import com.afterlight.madeproject.ui.theme.*
import com.afterlight.madeproject.utils.DateTimeUtils

@Composable
fun EventDetailScreen(
    onRecapClick: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: EventDetailViewModel = hiltViewModel()
) {
    val event by viewModel.event.collectAsStateWithLifecycle()
    val ticker by viewModel.ticker.collectAsStateWithLifecycle()
    val rsvpDone by viewModel.rsvpDone.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // DiceBear avatar for the host (API #1)
    val hostAvatarUrl = viewModel.externalApiService.diceBearAvatarUrl(
        event?.hostUid ?: "default-host"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Snow)
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // FIX: Back button is now properly clickable
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .neoBrutalism(backgroundColor = Pearl, cornerRadius = 8.dp)
                    .clickable(onClick = onBackClick),
                contentAlignment = Alignment.Center
            ) {
                Text("<", style = GatherTypography.titleLarge, color = Coal)
            }

            Text(
                text = "EVENT DETAILS",
                style = GatherTypography.labelLarge,
                color = Coal
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // FIX: Display real event data from ViewModel instead of hardcoded values
        if (event == null) {
            // Loading state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neoBrutalism(backgroundColor = Pearl, shadowOffset = 6.dp)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "LOADING EVENT DATA...",
                    style = GatherTypography.labelLarge,
                    color = LightTextMuted
                )
            }
        } else {
            val e = event!!

            // Magazine-Style Header — using real title
            Text(
                text = e.title.ifBlank { "Untitled Event" },
                style = GatherTypography.displayLarge,
                color = Coal
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Event Cover Image — uses Unsplash fallback (API #2) if no cover URL
            val coverUrl = e.coverImageUrl.ifBlank {
                "https://source.unsplash.com/800x600/?${e.category.ifBlank { "event,campus" }}"
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .neoBrutalism(backgroundColor = Sand, shadowOffset = 8.dp)
            ) {
                AsyncImage(
                    model = coverUrl,
                    contentDescription = e.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Host Info Row with DiceBear avatar (API #1)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .neoBrutalism(backgroundColor = Pearl, shadowOffset = 4.dp)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = hostAvatarUrl,
                    contentDescription = "Host avatar",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(2.dp, Coal, CircleShape),
                    contentScale = ContentScale.Crop
                )
                Column {
                    Text(
                        text = "HOSTED BY",
                        style = GatherTypography.labelSmall,
                        color = LightTextMuted
                    )
                    Text(
                        text = e.hostName.ifBlank { "Event Host" }.uppercase(),
                        style = GatherTypography.titleLarge,
                        color = Coal
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Metadata Pills — using real data
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetadataPill(text = e.venue.ifBlank { "TBD" }.uppercase())
                MetadataPill(text = DateTimeUtils.formatEventTime(e.dateTime).uppercase())
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetadataPill(text = "${e.spotsLeft} SPOTS LEFT")
                if (e.isPaid) {
                    MetadataPill(text = "₹${e.price}")
                } else {
                    MetadataPill(text = "FREE")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Body Copy — real description
            Text(
                text = e.description.ifBlank { "No description available for this event." },
                style = GatherTypography.bodyLarge,
                color = LightTextMuted
            )

            // Vibe Tags
            if (e.vibes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    e.vibes.forEach { vibe ->
                        Box(
                            modifier = Modifier
                                .neoBrutalism(backgroundColor = Coal, shadowOffset = 2.dp)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = vibe.name.uppercase(),
                                style = GatherTypography.labelSmall,
                                color = Snow
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Social Proof Ticker
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neoBrutalism(backgroundColor = Sand, shadowOffset = 2.dp)
                    .padding(12.dp)
            ) {
                Text(text = ticker, style = GatherTypography.labelMedium, color = Coal)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // RSVP Button
            BrutalistButton(
                text = if (rsvpDone) "RSVP CONFIRMED ✓" else "RSVP NOW",
                onClick = { if (!rsvpDone) viewModel.rsvp() },
                backgroundColor = if (rsvpDone) Moss else Copper,
                enabled = !rsvpDone,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Share Button (NFC / Link)
            val context = androidx.compose.ui.platform.LocalContext.current
            val activity = context as? android.app.Activity
            
            androidx.compose.runtime.DisposableEffect(e) {
                activity?.let { com.afterlight.madeproject.utils.NfcHelper.enableNfcSharing(it, e.eventId) }
                onDispose {
                    activity?.let { com.afterlight.madeproject.utils.NfcHelper.disableNfcSharing(it) }
                }
            }

            BrutalistButton(
                text = "SHARE (TAP NFC OR SEND LINK)",
                onClick = { activity?.let { com.afterlight.madeproject.utils.NfcHelper.shareEventGeneric(it, e.eventId, e.title) } },
                backgroundColor = Sand,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // View Recap button
            BrutalistButton(
                text = "VIEW RECAP",
                onClick = { onRecapClick(viewModel.eventId) },
                backgroundColor = Coal,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun MetadataPill(text: String) {
    Box(
        modifier = Modifier
            .border(2.dp, Coal, GatherShapes().pill)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = GatherTypography.labelMedium,
            color = Coal
        )
    }
}