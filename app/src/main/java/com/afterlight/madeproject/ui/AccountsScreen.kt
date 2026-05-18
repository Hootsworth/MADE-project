package com.afterlight.madeproject.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.afterlight.madeproject.domain.model.UserProfile
import com.afterlight.madeproject.ui.components.BrutalistButton
import com.afterlight.madeproject.ui.theme.Coal
import com.afterlight.madeproject.ui.theme.Copper
import com.afterlight.madeproject.ui.theme.GatherTypography
import com.afterlight.madeproject.ui.theme.LightGlass
import com.afterlight.madeproject.ui.theme.LightTextMuted
import com.afterlight.madeproject.ui.theme.Moss
import com.afterlight.madeproject.ui.theme.Pearl
import com.afterlight.madeproject.ui.theme.Sand
import com.afterlight.madeproject.ui.theme.Snow
import com.afterlight.madeproject.ui.theme.Wine
import com.afterlight.madeproject.ui.theme.neoBrutalism
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AccountsScreen(
    onBackClick: () -> Unit,
    onSignedOut: () -> Unit,
    onMyEventsClick: () -> Unit,
    onHostClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: AccountsViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val signingOut by viewModel.isSigningOut.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Snow)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

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
                modifier = Modifier.width(120.dp)
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "ACCOUNT CENTER", style = GatherTypography.labelLarge, color = Coal)
                Text(text = "Profile, access, identity", style = GatherTypography.bodyMedium, color = LightTextMuted)
            }
        }

        AccountHeroCard(profile = profile, state = state)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AccountStatCard(
                label = "Upcoming",
                value = state.upcomingCount.toString(),
                accent = Copper,
                modifier = Modifier.weight(1f)
            )
            AccountStatCard(
                label = "Hosted",
                value = state.hostedCount.toString(),
                accent = Moss,
                modifier = Modifier.weight(1f)
            )
            AccountStatCard(
                label = "Archive",
                value = state.pastCount.toString(),
                accent = Sand,
                modifier = Modifier.weight(1f)
            )
        }

        AccountPanel(title = "Profile Notes", kicker = "Identity") {
            AccountInfoRow(label = "Name", value = profile?.name?.ifBlank { "Unnamed user" } ?: "Loading...")
            AccountInfoRow(label = "Email", value = profile?.email?.ifBlank { "No email on file" } ?: "No email on file")
            AccountInfoRow(label = "Role", value = profile?.role?.name?.replaceFirstChar(Char::uppercase) ?: "Student")
            AccountInfoRow(label = "Department", value = profile?.department?.ifBlank { "Not set" } ?: "Not set")
            AccountInfoRow(label = "Year", value = profile?.year?.ifBlank { "Not set" } ?: "Not set")
            AccountInfoRow(label = "Mode", value = if (state.isAnonymous) "Test mode session" else "Verified account")
            AccountInfoRow(label = "Joined", value = profile?.createdAt?.let(::formatShortDate) ?: "Syncing...")
            AccountInfoRow(
                label = "Referral",
                value = profile?.referralCode?.ifBlank { "No referral code yet" } ?: "No referral code yet"
            )
        }

        AccountPanel(title = "Interests + Badges", kicker = "Signals") {
            Text(text = "INTERESTS", style = GatherTypography.labelMedium, color = Coal)
            AccountTagCloud(
                values = profile?.interests.orEmpty(),
                emptyLabel = "No interests selected yet"
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(text = "BADGES", style = GatherTypography.labelMedium, color = Coal)
            AccountTagCloud(
                values = profile?.badgesEarned.orEmpty(),
                emptyLabel = "No badges earned yet"
            )
        }

        AccountPanel(title = "Quick Actions", kicker = "Shortcuts") {
            AccountActionRow(
                icon = Icons.Outlined.CalendarMonth,
                title = "My Events",
                body = "Review your RSVPs, history, and hosted work.",
                accent = Pearl,
                onClick = onMyEventsClick
            )
            AccountActionRow(
                icon = Icons.Outlined.Celebration,
                title = "Host Something",
                body = "Jump straight into creating a new event.",
                accent = Sand,
                onClick = onHostClick
            )
            AccountActionRow(
                icon = Icons.Outlined.AutoAwesome,
                title = "AI Settings",
                body = "Tune provider, model, and app-side automation tools.",
                accent = LightGlass,
                onClick = onSettingsClick
            )
        }

        AccountPanel(title = "Danger Zone", kicker = "Access") {
            Text(
                text = "Signing out clears the current session on this device and returns you to authentication.",
                style = GatherTypography.bodyMedium,
                color = LightTextMuted
            )
            Spacer(modifier = Modifier.height(8.dp))
            BrutalistButton(
                text = if (signingOut) "Signing Out..." else "Sign Out",
                onClick = { viewModel.signOut(onSignedOut) },
                enabled = !signingOut,
                backgroundColor = Wine,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(96.dp))
    }
}

@Composable
private fun AccountHeroCard(profile: UserProfile?, state: AccountsUiState) {
    val displayName = profile?.name?.ifBlank { "Unnamed user" } ?: "Loading profile"
    val initials = displayName
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "?" }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neoBrutalism(backgroundColor = Pearl, shadowOffset = 8.dp)
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .neoBrutalism(backgroundColor = Copper, shadowOffset = 3.dp)
                        .padding(horizontal = 18.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = initials,
                        style = GatherTypography.titleLarge,
                        color = Snow
                    )
                }

                AccountBadge(
                    icon = if (state.isAnonymous) Icons.Outlined.PersonOutline else Icons.Outlined.Badge,
                    text = if (state.isAnonymous) "TEST MODE" else "LIVE ACCOUNT",
                    tint = if (state.isAnonymous) Sand else Moss
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "PROFILE CARD", style = GatherTypography.labelLarge, color = Coal)
                Text(text = displayName, style = GatherTypography.displayLarge, color = Coal)
                Text(
                    text = profile?.email?.ifBlank { "No email on file" } ?: "Connecting your account details...",
                    style = GatherTypography.bodyLarge,
                    color = LightTextMuted
                )
            }

            Text(
                text = buildString {
                    append("UID ")
                    append(state.uid.takeLast(8).ifBlank { "PENDING" })
                    append("  /  ")
                    append((profile?.department ?: "General").ifBlank { "General" }.uppercase())
                },
                style = GatherTypography.labelMedium,
                color = Coal
            )
        }
    }
}

@Composable
private fun AccountPanel(
    title: String,
    kicker: String,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neoBrutalism(backgroundColor = Snow, shadowOffset = 6.dp)
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = kicker.uppercase(), style = GatherTypography.labelMedium, color = LightTextMuted)
                Text(text = title, style = GatherTypography.titleLarge, color = Coal)
            }
            content()
        }
    }
}

@Composable
private fun AccountStatCard(
    label: String,
    value: String,
    accent: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .neoBrutalism(backgroundColor = Snow, shadowOffset = 4.dp)
            .padding(horizontal = 12.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = GatherTypography.headlineLarge, color = accent)
            Text(text = label.uppercase(), style = GatherTypography.labelMedium, color = Coal, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun AccountInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label.uppercase(), style = GatherTypography.labelMedium, color = LightTextMuted)
        Text(
            text = value,
            style = GatherTypography.bodyMedium,
            color = Coal,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun AccountTagCloud(values: List<String>, emptyLabel: String) {
    if (values.isEmpty()) {
        Text(text = emptyLabel, style = GatherTypography.bodyMedium, color = LightTextMuted)
        return
    }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        values.forEach { value ->
            Box(
                modifier = Modifier
                    .neoBrutalism(backgroundColor = Pearl, shadowOffset = 2.dp)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(text = value.uppercase(), style = GatherTypography.labelMedium, color = Coal)
            }
        }
    }
}

@Composable
private fun AccountActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String,
    accent: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neoBrutalism(backgroundColor = accent, shadowOffset = 3.dp)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = Coal)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = title, style = GatherTypography.labelLarge, color = Coal)
                    Text(text = body, style = GatherTypography.bodyMedium, color = LightTextMuted)
                }
            }
            Box(
                modifier = Modifier
                    .neoBrutalism(backgroundColor = Snow, shadowOffset = 2.dp)
                    .padding(horizontal = 10.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = title,
                    tint = Coal
                )
            }
        }
    }
}

@Composable
private fun AccountBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    tint: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier
            .neoBrutalism(backgroundColor = tint, shadowOffset = 2.dp)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Coal)
        Text(text = text, style = GatherTypography.labelMedium, color = Coal)
    }
}

private fun formatShortDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
