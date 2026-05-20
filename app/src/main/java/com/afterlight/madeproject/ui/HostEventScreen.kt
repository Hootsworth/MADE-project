package com.afterlight.madeproject.ui

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.afterlight.madeproject.domain.model.EventDraft
import com.afterlight.madeproject.domain.model.UserRole
import com.afterlight.madeproject.ui.components.SmoothButton
import com.afterlight.madeproject.ui.components.SmoothTextField
import com.afterlight.madeproject.utils.EditorialPosterRenderer
import com.afterlight.madeproject.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun HostEventScreen(
    onPublished: (String) -> Unit,
    viewModel: HostEventViewModel = hiltViewModel()
) {
    val draft by viewModel.draft.collectAsStateWithLifecycle()
    val aiLoading by viewModel.aiLoading.collectAsStateWithLifecycle()
    val aiReason by viewModel.aiReason.collectAsStateWithLifecycle()
    val publishError by viewModel.publishError.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    val pagerState = rememberPagerState { 3 }
    val scope = rememberCoroutineScope()
    val accentColor = Copper

    var posterBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showSuccess by remember { mutableStateOf(false) }

    val isHost = userProfile?.role == UserRole.HOST

    if (showSuccess) {
        PublishSuccessView(onPublished = { onPublished(draft.title) })
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Snow)
            .imePadding()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Phase Header
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(text = "Create Edition", style = GatherTypography.displayLarge, color = Coal)

            // Smooth Progress Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val phases = listOf("Intel", "Logistics", "Editorial")
                phases.forEachIndexed { index, _ ->
                    val isCurrent = pagerState.currentPage == index
                    val isPast = pagerState.currentPage > index

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(if (isCurrent || isPast) Coal else Pearl.copy(alpha = 0.6f))
                    )
                }
            }
            Text(
                text = when(pagerState.currentPage) {
                    0 -> "Step 1: Primary Intel"
                    1 -> "Step 2: Logistics & Vibe"
                    else -> "Step 3: Editorial Preview"
                },
                style = GatherTypography.labelMedium,
                color = LightTextMuted
            )
        }

        // Permission Warning
        if (!isHost && userProfile != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Wine.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Wine)
                    Text(
                        text = "Permission error: Your role is not set to 'Host'. Publishing will fail.",
                        style = GatherTypography.bodyMedium,
                        color = Wine
                    )
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            userScrollEnabled = false,
            beyondViewportPageCount = 1
        ) { page ->
            when (page) {
                0 -> HostEventStepBasic(draft, viewModel::updateDraft, viewModel::generateCoverImage, aiLoading)
                1 -> HostEventStepDetails(draft, viewModel::updateDraft)
                else -> {
                    HostEventPreview(
                        draft = draft,
                        posterBitmap = posterBitmap,
                        onBuildPoster = {
                            posterBitmap = EditorialPosterRenderer.renderPoster(
                                title = draft.title.ifBlank { "Untitled Event" },
                                subtitle = draft.category.ifBlank { "Campus Edition" },
                                accent = accentColor
                            )
                        }
                    )
                }
            }
        }

        // Bottom Actions
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(bottom = 32.dp)) {
            AnimatedVisibility(visible = aiReason != null && pagerState.currentPage < 2) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Sand.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Coal)
                        Text(text = "AI Hint: ${aiReason}", style = GatherTypography.bodyMedium, color = Coal)
                    }
                }
            }

            publishError?.let {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Wine.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "Error: $it",
                        style = GatherTypography.bodyMedium,
                        color = Wine,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (pagerState.currentPage > 0) {
                    SmoothButton(
                        text = "Back",
                        onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                        containerColor = Pearl.copy(alpha = 0.5f),
                        contentColor = Coal,
                        modifier = Modifier.weight(0.4f)
                    )
                }
                SmoothButton(
                    text = if (pagerState.currentPage < 2) "Continue" else "Publish Edition",
                    onClick = {
                        if (pagerState.currentPage < 2) {
                            viewModel.saveDraft()
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        } else {
                            viewModel.publish {
                                showSuccess = true
                            }
                        }
                    },
                    enabled = isHost || pagerState.currentPage < 2,
                    containerColor = Coal,
                    contentColor = Snow,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun HostEventStepBasic(
    draft: EventDraft,
    onDraftChange: (EventDraft) -> Unit,
    onGenerateCover: () -> Unit,
    aiLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
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
                Text("Primary Details", style = GatherTypography.titleLarge, color = Coal)

                SmoothTextField(
                    value = draft.title,
                    onValueChange = { onDraftChange(draft.copy(title = it)) },
                    label = "Event Title"
                )

                SmoothTextField(
                    value = draft.description,
                    onValueChange = { onDraftChange(draft.copy(description = it)) },
                    label = "Brief Description",
                    singleLine = false
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Cover Image", style = GatherTypography.labelMedium, color = Coal)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Pearl.copy(alpha = 0.3f))
                    ) {
                        if (draft.coverImageUrl.isNotBlank()) {
                            AsyncImage(
                                model = draft.coverImageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No image selected", style = GatherTypography.bodyMedium, color = LightTextMuted)
                            }
                        }
                    }
                    SmoothButton(
                        text = "Auto-Generate Cover",
                        onClick = onGenerateCover,
                        containerColor = Pearl.copy(alpha = 0.5f),
                        contentColor = Coal,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                SmoothTextField(
                    value = draft.category,
                    onValueChange = { onDraftChange(draft.copy(category = it)) },
                    label = "Category (e.g. Tech, Party)"
                )
            }
        }
    }
}

@Composable
private fun HostEventStepDetails(draft: EventDraft, onDraftChange: (EventDraft) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Pearl),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text("Logistics & Environment", style = GatherTypography.titleLarge, color = Coal)

                val context = androidx.compose.ui.platform.LocalContext.current
                val calendar = remember(draft.dateTime) { java.util.Calendar.getInstance().apply { timeInMillis = draft.dateTime } }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    SmoothButton(
                        text = android.text.format.DateFormat.format("MMM dd", calendar).toString(),
                        onClick = {
                            android.app.DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val newCal = java.util.Calendar.getInstance().apply { timeInMillis = draft.dateTime }
                                    newCal.set(year, month, dayOfMonth)
                                    onDraftChange(draft.copy(dateTime = newCal.timeInMillis))
                                },
                                calendar.get(java.util.Calendar.YEAR),
                                calendar.get(java.util.Calendar.MONTH),
                                calendar.get(java.util.Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        containerColor = Pearl.copy(alpha = 0.5f),
                        contentColor = Coal,
                        modifier = Modifier.weight(1f)
                    )

                    SmoothButton(
                        text = android.text.format.DateFormat.format("hh:mm a", calendar).toString(),
                        onClick = {
                            android.app.TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    val newCal = java.util.Calendar.getInstance().apply { timeInMillis = draft.dateTime }
                                    newCal.set(java.util.Calendar.HOUR_OF_DAY, hourOfDay)
                                    newCal.set(java.util.Calendar.MINUTE, minute)
                                    onDraftChange(draft.copy(dateTime = newCal.timeInMillis))
                                },
                                calendar.get(java.util.Calendar.HOUR_OF_DAY),
                                calendar.get(java.util.Calendar.MINUTE),
                                false
                            ).show()
                        },
                        containerColor = Pearl.copy(alpha = 0.5f),
                        contentColor = Coal,
                        modifier = Modifier.weight(1f)
                    )
                }

                SmoothTextField(
                    value = draft.venue,
                    onValueChange = { onDraftChange(draft.copy(venue = it)) },
                    label = "Venue Location"
                )

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        SmoothTextField(
                            value = draft.capacity,
                            onValueChange = { onDraftChange(draft.copy(capacity = it)) },
                            label = "Capacity"
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        SmoothTextField(
                            value = draft.price,
                            onValueChange = { onDraftChange(draft.copy(price = it)) },
                            label = "Price (0 for free)"
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Vibe Vectors", style = GatherTypography.labelMedium, color = Coal)
                    VibeChips(
                        selected = draft.vibes.map { it.name.lowercase() },
                        onToggle = { vibeStr ->
                            val vibes = com.afterlight.madeproject.domain.model.VibeTag.entries.toList()
                            val selectedVibe = vibes.find { it.name.lowercase() == vibeStr }
                            selectedVibe?.let {
                                val next = draft.vibes.toMutableSet()
                                if (next.contains(it)) next.remove(it) else next.add(it)
                                onDraftChange(draft.copy(vibes = next.toList()))
                            }
                        }
                    )
                }

                SmoothTextField(
                    value = draft.tags,
                    onValueChange = { onDraftChange(draft.copy(tags = it)) },
                    label = "Search Tags (comma separated)"
                )
            }
        }
    }
}

@Composable
private fun HostEventPreview(
    draft: EventDraft,
    posterBitmap: Bitmap?,
    onBuildPoster: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Pearl),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
        ) {
            Column {
                Box(modifier = Modifier.fillMaxWidth().height(220.dp).background(Coal)) {
                    if (draft.coverImageUrl.isNotBlank()) {
                        AsyncImage(
                            model = draft.coverImageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = Moss,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = draft.category,
                            style = GatherTypography.labelSmall,
                            color = Snow,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Text(
                        text = draft.title.ifBlank { "Untitled Edition" },
                        style = GatherTypography.displayLarge.copy(fontSize = 32.sp, lineHeight = 36.sp),
                        color = Coal
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Location", style = GatherTypography.labelSmall, color = LightTextMuted)
                            Text(draft.venue.ifBlank { "TBD" }, style = GatherTypography.titleLarge, color = Coal)
                        }
                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Capacity", style = GatherTypography.labelSmall, color = LightTextMuted)
                            Text("${draft.capacity} spots", style = GatherTypography.titleLarge, color = Copper)
                        }
                    }

                    HorizontalDivider(thickness = 1.dp, color = Pearl)

                    Text(
                        text = draft.description.ifBlank { "No description provided for this campus edition." },
                        style = GatherTypography.bodyLarge,
                        color = Coal.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Poster Generation
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
                Text("Poster Asset", style = GatherTypography.titleLarge, color = Coal)

                if (posterBitmap != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        Image(
                            bitmap = posterBitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Pearl.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No asset generated", style = GatherTypography.bodyMedium, color = LightTextMuted)
                    }
                }

                SmoothButton(
                    text = "Generate Print Asset",
                    onClick = onBuildPoster,
                    containerColor = Pearl.copy(alpha = 0.5f),
                    contentColor = Coal,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PublishSuccessView(onPublished: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Snow)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            AsyncImage(
                model = "https://images.unsplash.com/photo-1513151233558-d860c5398176?q=80&w=2070&auto=format&fit=crop",
                contentDescription = null,
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Edition Committed", style = GatherTypography.displayLarge, color = Coal)
                Text("Your event is now live in the registry.", style = GatherTypography.bodyLarge, color = Moss)
            }

            SmoothButton(
                text = "Return to Feed",
                onClick = onPublished,
                containerColor = Coal,
                contentColor = Snow,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}