package com.afterlight.madeproject.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.afterlight.madeproject.domain.model.EventAttendee
import com.afterlight.madeproject.ui.components.BrutalistButton
import com.afterlight.madeproject.ui.components.BrutalistTextField
import com.afterlight.madeproject.ui.theme.Coal
import com.afterlight.madeproject.ui.theme.GatherTypography
import com.afterlight.madeproject.ui.theme.LightTextMuted
import com.afterlight.madeproject.ui.theme.Moss
import com.afterlight.madeproject.ui.theme.Pearl
import com.afterlight.madeproject.ui.theme.Sand
import com.afterlight.madeproject.ui.theme.Snow
import com.afterlight.madeproject.ui.theme.neoBrutalism
import com.afterlight.madeproject.utils.DateTimeUtils

@Composable
fun HostControlsScreen(
    onBackClick: () -> Unit,
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
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BrutalistButton(
                text = "Back",
                onClick = onBackClick,
                backgroundColor = Snow,
                textColor = Coal,
                modifier = Modifier.fillMaxWidth(0.35f)
            )
            Text(text = "HOST CONTROLS", style = GatherTypography.labelLarge, color = Coal)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neoBrutalism(backgroundColor = Pearl, shadowOffset = 6.dp)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = event?.title?.ifBlank { "Event" }?.uppercase() ?: "LOADING EVENT...",
                    style = GatherTypography.titleLarge,
                    color = Coal
                )
                Text(
                    text = "Signed-up accounts: ${attendees.size}",
                    style = GatherTypography.labelMedium,
                    color = LightTextMuted
                )
                Text(
                    text = "Showing: ${filteredAttendees.size}",
                    style = GatherTypography.labelSmall,
                    color = LightTextMuted
                )
            }
        }

        status?.let {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neoBrutalism(backgroundColor = Sand, shadowOffset = 4.dp)
                    .padding(12.dp)
            ) {
                Text(text = it.uppercase(), style = GatherTypography.labelMedium, color = Coal)
            }
        }

        if (!isHost) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neoBrutalism(backgroundColor = Sand, shadowOffset = 4.dp)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Only the host can manage attendees for this event.",
                    style = GatherTypography.bodyLarge,
                    color = Coal
                )
            }
            return
        }

        BrutalistTextField(
            value = query,
            onValueChange = { query = it },
            label = "Search accounts"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AttendeeFilter.entries.forEach { option ->
                val selected = option == filter
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .neoBrutalism(
                            backgroundColor = if (selected) Coal else Snow,
                            shadowOffset = if (selected) 4.dp else 2.dp
                        )
                        .clickable { filter = option }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option.label,
                        style = GatherTypography.labelSmall,
                        color = if (selected) Snow else Coal
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (filteredAttendees.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .neoBrutalism(backgroundColor = Pearl, shadowOffset = 6.dp)
                            .padding(20.dp)
                    ) {
                        Text(
                            text = if (attendees.isEmpty()) "No one has signed up yet." else "No accounts match your filter.",
                            style = GatherTypography.bodyLarge,
                            color = LightTextMuted
                        )
                    }
                }
            }

            items(filteredAttendees, key = { it.uid }) { attendee ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neoBrutalism(backgroundColor = Pearl, shadowOffset = 4.dp)
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = attendee.name.ifBlank { "Student" }.uppercase(),
                            style = GatherTypography.titleLarge,
                            color = Coal
                        )
                        if (attendee.email.isNotBlank()) {
                            Text(
                                text = attendee.email,
                                style = GatherTypography.bodyLarge,
                                color = LightTextMuted
                            )
                        }
                        Text(
                            text = "RSVP: ${DateTimeUtils.formatEventTime(attendee.rsvpAt)}",
                            style = GatherTypography.labelMedium,
                            color = Coal
                        )
                        Text(
                            text = if (attendee.checkInStatus) "STATUS: CHECKED IN" else "STATUS: NOT CHECKED IN",
                            style = GatherTypography.labelSmall,
                            color = LightTextMuted
                        )
                        BrutalistButton(
                            text = if (kickingUid == attendee.uid) "Removing..." else "Kick Account",
                            onClick = { confirmKickFor = attendee },
                            enabled = kickingUid != attendee.uid,
                            backgroundColor = Moss,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        confirmKickFor?.let { attendee ->
            AlertDialog(
                onDismissRequest = { confirmKickFor = null },
                title = { Text(text = "Confirm removal") },
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
                        Text(text = "Remove")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { confirmKickFor = null }) {
                        Text(text = "Cancel")
                    }
                }
            )
        }
    }
}

private enum class AttendeeFilter(val label: String) {
    ALL("ALL"),
    CHECKED_IN("CHECKED"),
    NOT_CHECKED_IN("PENDING")
}
