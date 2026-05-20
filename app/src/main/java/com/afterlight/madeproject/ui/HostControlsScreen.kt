package com.afterlight.madeproject.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.afterlight.madeproject.domain.model.EventAttendee
import com.afterlight.madeproject.ui.components.SmoothButton
import com.afterlight.madeproject.ui.components.SmoothTextField
import com.afterlight.madeproject.ui.theme.Coal
import com.afterlight.madeproject.ui.theme.GatherTypography
import com.afterlight.madeproject.ui.theme.LightTextMuted
import com.afterlight.madeproject.ui.theme.Moss
import com.afterlight.madeproject.ui.theme.Pearl
import com.afterlight.madeproject.ui.theme.Sand
import com.afterlight.madeproject.ui.theme.Snow
import com.afterlight.madeproject.ui.theme.Wine
import com.afterlight.madeproject.utils.DateTimeUtils

@Composable
fun HostControlsScreen(
    onBackClick: () -> Unit,
    onCheckInClick: (String) -> Unit,
    viewModel: HostControlsViewModel = hiltViewModel()
) {
    val event by viewModel.event.collectAsStateWithLifecycle()
    val attendees by viewModel.attendees.collectAsStateWithLifecycle()
    val isHost by viewModel.isHost.collectAsStateWithLifecycle()
    val kickingUid by viewModel.kickingUid.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(AttendeeFilter.ALL) }
    var confirmKickFor by remember { mutableStateOf<EventAttendee?>(null) }

    val filteredAttendees = remember(attendees, query, filter) {
        attendees.filter { attendee ->
            val matchesQuery = query.isBlank() ||
                    attendee.name.contains(query, ignoreCase = true) ||
                    attendee.email.contains(query, ignoreCase = true)
            val matchesFilter = when (filter) {
                AttendeeFilter.ALL -> true
                AttendeeFilter.CHECKED_IN -> attendee.checkInStatus
                AttendeeFilter.NOT_CHECKED_IN -> !attendee.checkInStatus
            }
            matchesQuery && matchesFilter
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Snow)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmoothButton(
                text = "Back",
                onClick = onBackClick,
                containerColor = Pearl.copy(alpha = 0.5f),
                contentColor = Coal,
                modifier = Modifier.fillMaxWidth(0.3f)
            )
            Text(text = "Host Center", style = GatherTypography.titleLarge, color = Coal)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Pearl),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = event?.title?.ifBlank { "Event" } ?: "Loading event...",
                    style = GatherTypography.titleLarge,
                    color = Coal
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Pearl.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Signed up: ${attendees.size} / ${event?.capacity ?: 0}",
                            style = GatherTypography.bodyMedium,
                            color = Coal
                        )
                        Text(
                            text = "Total RSVPs: ${event?.rsvpCount ?: attendees.size}",
                            style = GatherTypography.bodyMedium,
                            color = Coal
                        )
                    }
                }

                if (event != null) {
                    SmoothButton(
                        text = "Open QR Check-In",
                        onClick = { onCheckInClick(event!!.eventId) },
                        containerColor = Coal,
                        contentColor = Snow,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        status?.let {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Sand.copy(alpha = 0.2f)
            ) {
                Text(
                    text = it,
                    style = GatherTypography.bodyMedium,
                    color = Coal,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        if (!isHost) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Wine.copy(alpha = 0.1f)
            ) {
                Text(
                    text = "Only the host can manage attendees for this event.",
                    style = GatherTypography.bodyLarge,
                    color = Wine,
                    modifier = Modifier.padding(16.dp)
                )
            }
            return
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SmoothTextField(
                value = query,
                onValueChange = { query = it },
                label = "Search accounts"
            )

            // Segmented Control Pill
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                color = Pearl.copy(alpha = 0.4f)
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    AttendeeFilter.entries.forEach { option ->
                        val selected = option == filter
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(CircleShape)
                                .background(if (selected) Pearl else Color.Transparent)
                                .clickable { filter = option }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = option.label,
                                style = GatherTypography.labelMedium,
                                color = if (selected) Coal else LightTextMuted
                            )
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 64.dp)
        ) {
            if (filteredAttendees.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Transparent
                    ) {
                        Text(
                            text = if (attendees.isEmpty()) "No one has signed up yet." else "No accounts match your filter.",
                            style = GatherTypography.bodyLarge,
                            color = LightTextMuted,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }
            }

            items(filteredAttendees, key = { it.uid }) { attendee ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Pearl),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = attendee.name.ifBlank { "Student" },
                                    style = GatherTypography.titleLarge,
                                    color = Coal
                                )
                                if (attendee.email.isNotBlank()) {
                                    Text(
                                        text = attendee.email,
                                        style = GatherTypography.bodyMedium,
                                        color = LightTextMuted
                                    )
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                color = if (attendee.checkInStatus) Moss.copy(alpha = 0.1f) else Pearl.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = if (attendee.checkInStatus) "Checked In" else "Pending",
                                    style = GatherTypography.labelSmall,
                                    color = if (attendee.checkInStatus) Moss else LightTextMuted,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Text(
                            text = "RSVP: ${DateTimeUtils.formatEventTime(attendee.rsvpAt)}",
                            style = GatherTypography.bodyMedium,
                            color = Coal
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (!attendee.checkInStatus) {
                                SmoothButton(
                                    text = "Check In",
                                    onClick = { viewModel.checkInAttendee(attendee.uid) },
                                    containerColor = Moss.copy(alpha = 0.15f),
                                    contentColor = Moss,
                                    modifier = Modifier.weight(1f).height(44.dp)
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                            SmoothButton(
                                text = if (kickingUid == attendee.uid) "Removing..." else "Remove",
                                onClick = { confirmKickFor = attendee },
                                enabled = kickingUid != attendee.uid,
                                containerColor = Wine.copy(alpha = 0.1f),
                                contentColor = Wine,
                                modifier = Modifier.weight(1f).height(44.dp)
                            )
                        }
                    }
                }
            }
        }

        confirmKickFor?.let { attendee ->
            AlertDialog(
                onDismissRequest = { confirmKickFor = null },
                title = { Text(text = "Confirm removal", style = GatherTypography.titleLarge) },
                text = {
                    Text(
                        text = "Remove ${attendee.name.ifBlank { "this account" }} from this event? They will lose their RSVP.",
                        style = GatherTypography.bodyLarge,
                        color = Coal
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.kickAttendee(attendee)
                        confirmKickFor = null
                    }) {
                        Text(text = "Remove", color = Wine)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { confirmKickFor = null }) {
                        Text(text = "Cancel", color = Coal)
                    }
                },
                containerColor = Pearl
            )
        }
    }
}

private enum class AttendeeFilter(val label: String) {
    ALL("All"),
    CHECKED_IN("Checked In"),
    NOT_CHECKED_IN("Pending")
}