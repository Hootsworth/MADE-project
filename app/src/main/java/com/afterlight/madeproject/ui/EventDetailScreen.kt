package com.afterlight.madeproject.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.afterlight.madeproject.domain.model.EventStatus
import com.afterlight.madeproject.ui.components.SmoothButton
import com.afterlight.madeproject.ui.theme.Coal
import com.afterlight.madeproject.ui.theme.Copper
import com.afterlight.madeproject.ui.theme.GatherTypography
import com.afterlight.madeproject.ui.theme.LightTextMuted
import com.afterlight.madeproject.ui.theme.Moss
import com.afterlight.madeproject.ui.theme.Pearl
import com.afterlight.madeproject.ui.theme.Sand
import com.afterlight.madeproject.ui.theme.Snow
import com.afterlight.madeproject.utils.DateTimeUtils

@Composable
fun EventDetailScreen(
    onRecapClick: (String) -> Unit,
    onPayClick: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: EventDetailViewModel = hiltViewModel()
) {
    val event by viewModel.event.collectAsStateWithLifecycle()
    val ticker by viewModel.ticker.collectAsStateWithLifecycle()
    val rsvpDone by viewModel.rsvpDone.collectAsStateWithLifecycle()
    val hasRsvped by viewModel.hasRsvped.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()
    val waitlistPosition by viewModel.waitlistPosition.collectAsStateWithLifecycle()
    val myTicketId by viewModel.myTicketId.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? Activity
    val scrollState = rememberScrollState()

    val hostAvatarUrl = viewModel.externalApiService.diceBearAvatarUrl(event?.hostUid ?: "default-host")
    val rsvpConfirmed = rsvpDone || hasRsvped

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Snow)
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Pearl.copy(alpha = 0.5f),
                modifier = Modifier
                    .size(48.dp)
                    .clickable(onClick = onBackClick)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.ChevronLeft, contentDescription = "Back", tint = Coal)
                }
            }

            Text(text = "Details", style = GatherTypography.labelLarge, color = Coal)
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (event == null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Pearl.copy(alpha = 0.3f)
            ) {
                Box(
                    modifier = Modifier.padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Loading event data...",
                        style = GatherTypography.bodyLarge,
                        color = LightTextMuted
                    )
                }
            }
        } else {
            val e = event!!
            val eventOver = e.status == EventStatus.PAST || e.dateTime <= System.currentTimeMillis()
            val coverUrl = e.coverImageUrl.ifBlank {
                "https://source.unsplash.com/800x600/?${e.category.ifBlank { "event,campus" }}"
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Pearl),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = coverUrl,
                        contentDescription = e.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = ColorPainter(Sand),
                        error = ColorPainter(Pearl)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Snow.copy(alpha = 0.92f)),
                                    startY = 180f
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(shape = CircleShape, color = Sand.copy(alpha = 0.9f)) {
                            Text(
                                text = if (e.isPaid) "Paid Edition" else "Open Edition",
                                style = GatherTypography.labelSmall,
                                color = Coal,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                        Text(
                            text = e.title.ifBlank { "Untitled Event" },
                            style = GatherTypography.displayLarge,
                            color = Coal
                        )
                        Text(
                            text = DateTimeUtils.formatEventTime(e.dateTime),
                            style = GatherTypography.bodyLarge,
                            color = LightTextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                SmoothMetadataPill(text = e.venue.ifBlank { "TBD" })
                SmoothMetadataPill(text = "${e.spotsLeft} spots left", backgroundColor = Sand.copy(alpha = 0.2f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                SmoothMetadataPill(text = e.category.ifBlank { "Campus" })
                if (e.isPaid) {
                    SmoothMetadataPill(text = "₹${e.price.ifBlank { "0" }}", backgroundColor = Copper.copy(alpha = 0.15f))
                } else {
                    SmoothMetadataPill(text = "Free", backgroundColor = Moss.copy(alpha = 0.15f))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Pearl,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = hostAvatarUrl,
                        contentDescription = "Host avatar",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Column {
                        Text(text = "Hosted by", style = GatherTypography.labelSmall, color = LightTextMuted)
                        Text(text = e.hostName.ifBlank { "Event Host" }, style = GatherTypography.titleLarge, color = Coal)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Pearl.copy(alpha = 0.4f)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = "About the event", style = GatherTypography.titleLarge, color = Coal)
                    Text(
                        text = e.description.ifBlank { "No description available for this event." },
                        style = GatherTypography.bodyLarge,
                        color = Coal.copy(alpha = 0.8f)
                    )
                }
            }

            if (e.vibes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = Pearl.copy(alpha = 0.35f)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = "Atmosphere", style = GatherTypography.titleLarge, color = Coal)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            e.vibes.forEach { vibe ->
                                Surface(shape = CircleShape, color = Sand.copy(alpha = 0.2f)) {
                                    Text(
                                        text = vibe.name.lowercase().replaceFirstChar { it.uppercase() },
                                        style = GatherTypography.labelMedium,
                                        color = Coal,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Sand.copy(alpha = 0.18f)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "Live signal", style = GatherTypography.labelSmall, color = Moss)
                    Text(text = ticker, style = GatherTypography.bodyMedium, color = Coal)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (eventOver) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Pearl.copy(alpha = 0.4f)
                ) {
                    Text(
                        text = "This event has ended. RSVP is closed.",
                        style = GatherTypography.bodyMedium,
                        color = Coal,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                SmoothButton(
                    text = "View Recap",
                    onClick = { onRecapClick(viewModel.eventId) },
                    containerColor = Coal,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                SmoothButton(
                    text = when {
                        rsvpConfirmed -> "RSVP Confirmed ✓"
                        e.isPaid -> "Pay ₹${e.price.ifBlank { "0" }} & RSVP"
                        else -> "RSVP Now"
                    },
                    onClick = {
                        if (!rsvpConfirmed) {
                            if (e.isPaid) onPayClick(e.eventId) else viewModel.rsvp()
                        }
                    },
                    containerColor = if (rsvpConfirmed) Moss else Coal,
                    contentColor = Snow,
                    enabled = !rsvpConfirmed,
                    modifier = Modifier.fillMaxWidth()
                )

                status?.let { msg ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Sand.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = msg,
                            style = GatherTypography.bodyMedium,
                            color = Coal,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                waitlistPosition?.let { pos ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Pearl.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = "You are #$pos on the waitlist",
                            style = GatherTypography.bodyMedium,
                            color = Coal,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                if (waitlistPosition != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    SmoothButton(
                        text = "Cancel Waitlist",
                        onClick = { viewModel.cancelWaitlist() },
                        containerColor = Pearl,
                        contentColor = Coal,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (e.eventId.isNotBlank()) {
                    DisposableEffect(e.eventId) {
                        activity?.let { com.afterlight.madeproject.utils.NfcHelper.enableNfcSharing(it, e.eventId) }
                        onDispose {
                            activity?.let { com.afterlight.madeproject.utils.NfcHelper.disableNfcSharing(it) }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    SmoothButton(
                        text = "Share",
                        onClick = { activity?.let { com.afterlight.madeproject.utils.NfcHelper.shareEventGeneric(it, e.eventId, e.title) } },
                        containerColor = Pearl.copy(alpha = 0.5f),
                        contentColor = Coal,
                        modifier = Modifier.weight(1f)
                    )
                    SmoothButton(
                        text = "Recaps",
                        onClick = { onRecapClick(viewModel.eventId) },
                        containerColor = Pearl.copy(alpha = 0.5f),
                        contentColor = Coal,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            myTicketId?.let { ticket ->
                if (ticket.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    val ticketUrl = remember(ticket) {
                        viewModel.externalApiService.qrCodeUrl("https://paperlike.app/events/${viewModel.eventId}?ticket=$ticket")
                    }
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        AsyncImage(
                            model = ticketUrl,
                            contentDescription = "Your ticket QR",
                            modifier = Modifier
                                .size(160.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SmoothMetadataPill(
    text: String,
    backgroundColor: Color = Pearl.copy(alpha = 0.4f)
) {
    Surface(
        shape = CircleShape,
        color = backgroundColor
    ) {
        Text(
            text = text,
            style = GatherTypography.labelMedium,
            color = Coal,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}