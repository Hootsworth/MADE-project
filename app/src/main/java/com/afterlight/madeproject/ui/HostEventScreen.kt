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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.afterlight.madeproject.domain.model.EventDraft
import com.afterlight.madeproject.domain.model.UserRole
import com.afterlight.madeproject.ui.components.BrutalistButton
import com.afterlight.madeproject.ui.components.BrutalistTextField
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

    val isHost = com.afterlight.madeproject.BuildConfig.DEBUG || userProfile?.role == UserRole.HOST

    if (showSuccess) {
        PublishSuccessView(onPublished = { onPublished(draft.title) }) // Simplified for demo
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Snow)
            .imePadding()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Phase Header
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "CREATE EDITION.", style = GatherTypography.displayLarge, color = Coal)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val phases = listOf("INTEL", "LOGISTICS", "EDITORIAL")
                phases.forEachIndexed { index, name ->
                    val isCurrent = pagerState.currentPage == index
                    val isPast = pagerState.currentPage > index
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .background(if (isCurrent || isPast) Coal else LightGlass)
                    )
                }
            }
            Text(
                text = when(pagerState.currentPage) {
                    0 -> "PHASE 01: PRIMARY INTEL"
                    1 -> "PHASE 02: LOGISTICS & VIBE"
                    else -> "PHASE 03: EDITORIAL PREVIEW"
                },
                style = GatherTypography.labelSmall,
                color = LightTextMuted
            )
        }

        // Permission Warning
        if (!isHost && userProfile != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neoBrutalism(backgroundColor = Wine, shadowOffset = 4.dp)
                    .padding(16.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Snow)
                    Text(
                        text = "PERMISSION ERROR: YOUR ROLE IS NOT SET TO 'HOST'. PUBLISHING WILL FAIL.",
                        style = GatherTypography.labelSmall,
                        color = Snow
                    )
                }
            }
        }

        // Pager
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
            // AI Suggestion Box
            AnimatedVisibility(visible = aiReason != null && pagerState.currentPage < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neoBrutalism(backgroundColor = Sand, shadowOffset = 4.dp)
                        .padding(16.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Coal)
                        Text(text = "AI INTEL: ${aiReason}", style = GatherTypography.labelSmall, color = Coal)
                    }
                }
            }

            // Error Display
            publishError?.let {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neoBrutalism(backgroundColor = Wine, shadowOffset = 4.dp)
                        .padding(16.dp)
                ) {
                    Text(text = "REGISTRY ERROR: $it", style = GatherTypography.labelSmall, color = Snow)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (pagerState.currentPage > 0) {
                    BrutalistButton(
                        text = "BACK",
                        onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                        backgroundColor = Snow,
                        textColor = Coal,
                        modifier = Modifier.weight(0.4f)
                    )
                }
                BrutalistButton(
                    text = if (pagerState.currentPage < 2) "CONTINUE" else "COMMIT EDITION",
                    onClick = {
                        if (pagerState.currentPage < 2) {
                            viewModel.saveDraft()
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        } else {
                            viewModel.publish { 
                                showSuccess = true
                                // We'll navigate after showing success for a bit
                            }
                        }
                    },
                    enabled = isHost || pagerState.currentPage < 2,
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neoBrutalism(backgroundColor = Pearl, shadowOffset = 8.dp)
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text("PRIMARY INTEL", style = GatherTypography.labelLarge, color = Coal)
                
                BrutalistTextField(
                    value = draft.title, 
                    onValueChange = { onDraftChange(draft.copy(title = it)) }, 
                    label = "EVENT TITLE"
                )
                
                BrutalistTextField(
                    value = draft.description, 
                    onValueChange = { onDraftChange(draft.copy(description = it)) }, 
                    label = "BRIEF DESCRIPTION",
                    singleLine = false
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("COVER IMAGE", style = GatherTypography.labelMedium, color = Coal)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .neoBrutalism(backgroundColor = LightGlass, shadowOffset = 4.dp)
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
                                Text("NO IMAGE SELECTED", style = GatherTypography.labelSmall, color = LightTextMuted)
                            }
                        }
                    }
                    BrutalistButton(
                        text = "AUTO-GENERATE FROM CATEGORY",
                        onClick = onGenerateCover,
                        backgroundColor = Snow,
                        textColor = Coal,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                BrutalistTextField(
                    value = draft.category, 
                    onValueChange = { onDraftChange(draft.copy(category = it)) }, 
                    label = "CATEGORY (E.G. TECH, PARTY, STUDY)"
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neoBrutalism(backgroundColor = Sand, shadowOffset = 8.dp)
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                Text("LOGISTICS & ENVIRONMENT", style = GatherTypography.labelLarge, color = Coal)
                
                val context = androidx.compose.ui.platform.LocalContext.current
                val calendar = remember(draft.dateTime) { java.util.Calendar.getInstance().apply { timeInMillis = draft.dateTime } }
                
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    BrutalistButton(
                        text = "DATE: ${android.text.format.DateFormat.format("MMM dd", calendar).toString().uppercase()}",
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
                        backgroundColor = Pearl,
                        textColor = Coal,
                        modifier = Modifier.weight(1f)
                    )

                    BrutalistButton(
                        text = "TIME: ${android.text.format.DateFormat.format("hh:mm a", calendar).toString().uppercase()}",
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
                        backgroundColor = Pearl,
                        textColor = Coal,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                BrutalistTextField(
                    value = draft.venue, 
                    onValueChange = { onDraftChange(draft.copy(venue = it)) }, 
                    label = "VENUE / LOCATION"
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        BrutalistTextField(
                            value = draft.capacity, 
                            onValueChange = { onDraftChange(draft.copy(capacity = it)) }, 
                            label = "CAPACITY"
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        BrutalistTextField(
                            value = draft.price, 
                            onValueChange = { onDraftChange(draft.copy(price = it)) }, 
                            label = "PRICE (0 FOR FREE)"
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("VIBE VECTORS", style = GatherTypography.labelMedium, color = Coal)
                    VibeChips(
                        selected = draft.vibes.map { it.name.lowercase() },
                        onToggle = { vibeStr ->
                            // Simple mapping back to enum for now
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

                BrutalistTextField(
                    value = draft.tags, 
                    onValueChange = { onDraftChange(draft.copy(tags = it)) }, 
                    label = "SEARCH TAGS (COMMA SEPARATED)"
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
        Text("EDITORIAL REVIEW", style = GatherTypography.labelLarge, color = Coal)

        // The Magazine-style Preview Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neoBrutalism(backgroundColor = Pearl, shadowOffset = 12.dp)
        ) {
            Column {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(Coal)) {
                    if (draft.coverImageUrl.isNotBlank()) {
                        AsyncImage(
                            model = draft.coverImageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .neoBrutalism(backgroundColor = Moss, shadowOffset = 2.dp)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = draft.category.uppercase(), style = GatherTypography.labelSmall, color = Snow)
                    }
                }
                
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = draft.title.ifBlank { "UNTITLED EDITION" }.uppercase(),
                        style = GatherTypography.displayLarge.copy(fontSize = 32.sp, lineHeight = 36.sp),
                        color = Coal
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("LOCATION", style = GatherTypography.labelSmall, color = LightTextMuted)
                            Text(draft.venue.ifBlank { "TBD" }, style = GatherTypography.labelLarge, color = Coal)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("CAPACITY", style = GatherTypography.labelSmall, color = LightTextMuted)
                            Text("${draft.capacity} SPOTS", style = GatherTypography.labelLarge, color = Copper)
                        }
                    }
                    
                    HorizontalDivider(thickness = 2.dp, color = Coal.copy(alpha = 0.1f))
                    
                    Text(
                        text = draft.description.ifBlank { "No description provided for this campus edition." },
                        style = GatherTypography.bodyLarge,
                        color = Coal
                    )
                }
            }
        }

        // Poster Generation Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neoBrutalism(backgroundColor = Snow, shadowOffset = 8.dp)
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text("POSTER ASSET", style = GatherTypography.labelLarge, color = Coal)
                
                if (posterBitmap != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                            .border(3.dp, Coal)
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
                            .background(LightGlass),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("NO ASSET GENERATED", style = GatherTypography.labelSmall, color = LightTextMuted)
                    }
                }

                BrutalistButton(
                    text = "RE-GENERATE ASSET",
                    onClick = onBuildPoster,
                    backgroundColor = Coal,
                    textColor = Snow,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
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
                modifier = Modifier.size(280.dp).neoBrutalism(shadowOffset = 12.dp)
            )
            
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("EDITION COMMITTED.", style = GatherTypography.displayLarge, color = Coal)
                Text("YOUR EVENT IS NOW LIVE IN THE REGISTRY", style = GatherTypography.labelMedium, color = Moss)
            }
            
            BrutalistButton(
                text = "RETURN TO FEED",
                onClick = onPublished,
                backgroundColor = Coal,
                textColor = Snow,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}