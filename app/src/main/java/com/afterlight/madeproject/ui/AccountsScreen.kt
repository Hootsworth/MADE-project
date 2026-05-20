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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.afterlight.madeproject.domain.model.UserProfile
import com.afterlight.madeproject.domain.model.UserRole
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

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
    val canHost = profile?.role == UserRole.HOST

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Snow)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

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
                modifier = Modifier.width(100.dp)
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "Account", style = GatherTypography.titleLarge, color = Coal)
                Text(text = "Profile & preferences", style = GatherTypography.bodyMedium, color = LightTextMuted)
            }
        }

        AccountHeroCard(profile = profile, state = state)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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

        AccountPanel(title = "Profile Identity") {
            AccountInfoRow(label = "Name", value = profile?.name?.ifBlank { "Unnamed user" } ?: "Loading...")
            AccountInfoRow(label = "Email", value = profile?.email?.ifBlank { "No email on file" } ?: "No email on file")
            AccountInfoRow(label = "Role", value = profile?.role?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Student")
            AccountInfoRow(label = "School", value = profile?.department?.ifBlank { "Not set" } ?: "Not set")
            AccountInfoRow(label = "Year", value = profile?.year?.ifBlank { "Not set" } ?: "Not set")
            AccountInfoRow(label = "Mode", value = if (state.isAnonymous) "Test mode session" else "Verified account")
            AccountInfoRow(label = "Joined", value = profile?.createdAt?.let(::formatShortDate) ?: "Syncing...")
            AccountInfoRow(label = "Referral", value = profile?.referralCode?.ifBlank { "None" } ?: "None")
        }

        AccountPanel(title = "Signals & Interests") {
            Text(text = "Interests", style = GatherTypography.labelMedium, color = LightTextMuted)
            AccountTagCloud(
                values = profile?.interests.orEmpty(),
                emptyLabel = "No interests selected yet"
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Badges", style = GatherTypography.labelMedium, color = LightTextMuted)
            AccountTagCloud(
                values = profile?.badgesEarned.orEmpty(),
                emptyLabel = "No badges earned yet"
            )
        }

        AccountPanel(title = "Shortcuts") {
            AccountActionRow(
                icon = Icons.Outlined.CalendarMonth,
                title = "My Events",
                body = "Review RSVPs and history",
                accent = Pearl,
                onClick = onMyEventsClick
            )
            if (canHost) {
                AccountActionRow(
                    icon = Icons.Outlined.Celebration,
                    title = "Host Something",
                    body = "Create a new event",
                    accent = Sand,
                    onClick = onHostClick
                )
            }
            AccountActionRow(
                icon = Icons.Outlined.AutoAwesome,
                title = "AI Settings",
                body = "Tune automation tools",
                accent = LightGlass,
                onClick = onSettingsClick
            )
        }

        AccountPanel(title = "Access") {
            Text(
                text = "Signing out clears the current session on this device.",
                style = GatherTypography.bodyMedium,
                color = LightTextMuted
            )
            Spacer(modifier = Modifier.height(12.dp))
            SmoothButton(
                text = if (signingOut) "Signing Out..." else "Sign Out",
                onClick = { viewModel.signOut(onSignedOut) },
                enabled = !signingOut,
                containerColor = Wine.copy(alpha = 0.1f),
                contentColor = Wine,
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Pearl),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Copper.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        style = GatherTypography.titleLarge,
                        color = Copper
                    )
                }

                AccountBadge(
                    icon = if (state.isAnonymous) Icons.Outlined.PersonOutline else Icons.Outlined.Badge,
                    text = if (state.isAnonymous) "Test Mode" else "Live",
                    tint = if (state.isAnonymous) Sand else Moss
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = displayName, style = GatherTypography.displayLarge, color = Coal)
                Text(
                    text = profile?.email?.ifBlank { "No email on file" } ?: "Connecting details...",
                    style = GatherTypography.bodyLarge,
                    color = LightTextMuted
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Pearl.copy(alpha = 0.3f)
            ) {
                Text(
                    text = "UID ${state.uid.takeLast(8).ifBlank { "PENDING" }} • ${(profile?.department ?: "General").ifBlank { "General" }}",
                    style = GatherTypography.labelMedium,
                    color = Coal,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun AccountPanel(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Pearl),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(text = title, style = GatherTypography.titleLarge, color = Coal)
            content()
        }
    }
}

@Composable
private fun AccountStatCard(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(112.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.1f)),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = GatherTypography.headlineLarge, color = accent)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, style = GatherTypography.labelMedium, color = Coal.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun AccountInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = GatherTypography.bodyMedium,
            color = LightTextMuted,
            modifier = Modifier.weight(0.38f)
        )
        Text(
            text = value,
            style = GatherTypography.bodyLarge,
            color = Coal,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.62f)
        )
    }
}

@Composable
private fun AccountTagCloud(values: List<String>, emptyLabel: String) {
    if (values.isEmpty()) {
        Text(text = emptyLabel, style = GatherTypography.bodyMedium, color = LightTextMuted)
        return
    }

    Layout(
        content = {
            values.forEach { value ->
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Pearl.copy(alpha = 0.4f)
                ) {
                    Text(
                        text = value,
                        style = GatherTypography.labelMedium,
                        color = Coal,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }
    ) { measurables, constraints ->
        val horizontalSpacing = 8.dp.roundToPx()
        val verticalSpacing = 8.dp.roundToPx()
        val placeables = measurables.map { it.measure(constraints) }

        var xPosition = 0
        var yPosition = 0
        var rowHeight = 0
        val positions = mutableListOf<Pair<Int, Int>>()

        placeables.forEach { placeable ->
            if (xPosition + placeable.width > constraints.maxWidth && xPosition != 0) {
                xPosition = 0
                yPosition += rowHeight + verticalSpacing
                rowHeight = 0
            }
            positions.add(Pair(xPosition, yPosition))
            xPosition += placeable.width + horizontalSpacing
            rowHeight = max(rowHeight, placeable.height)
        }

        layout(constraints.maxWidth, yPosition + rowHeight) {
            placeables.forEachIndexed { index, placeable ->
                val (x, y) = positions[index]
                placeable.placeRelative(x = x, y = y)
            }
        }
    }
}

@Composable
private fun AccountActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String,
    accent: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = Coal)
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = title, style = GatherTypography.labelLarge, color = Coal)
                    Text(text = body, style = GatherTypography.bodyMedium, color = LightTextMuted)
                }
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = LightTextMuted
            )
        }
    }
}

@Composable
private fun AccountBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    tint: Color
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = tint.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
            Text(text = text, style = GatherTypography.labelMedium, color = tint)
        }
    }
}

@Composable
fun SmoothButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Moss,
    contentColor: Color = Snow,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
    ) {
        Text(text, style = GatherTypography.labelLarge)
    }
}

private fun formatShortDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
    return formatter.format(Date(timestamp))
}