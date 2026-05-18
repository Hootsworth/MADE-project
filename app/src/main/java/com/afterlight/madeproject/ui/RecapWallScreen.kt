package com.afterlight.madeproject.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.afterlight.madeproject.ui.components.BrutalistButton
import com.afterlight.madeproject.ui.components.BrutalistTextField
import com.afterlight.madeproject.ui.theme.*

@Composable
fun RecapWallScreen(viewModel: RecapWallViewModel = hiltViewModel()) {
    val posts by viewModel.posts.collectAsStateWithLifecycle()
    val aiBusy by viewModel.aiBusy.collectAsStateWithLifecycle()
    var caption by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Snow)
            .imePadding()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Column(
            modifier = Modifier.padding(top = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "RECAP WALL.", style = GatherTypography.displayLarge, color = Coal)
            Text(text = "DOCUMENTATION", style = GatherTypography.labelMedium, color = LightTextMuted)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neoBrutalism(backgroundColor = Pearl, shadowOffset = 8.dp)
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text(
                    text = "LOG MOMENT",
                    style = GatherTypography.labelLarge,
                    color = Coal
                )

                BrutalistTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = "ADD ARCHIVE NOTE"
                )

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    BrutalistButton(
                        text = if (aiBusy) "POLISHING..." else "AI POLISH",
                        onClick = { viewModel.polishCaption(caption) { caption = it } },
                        enabled = caption.isNotBlank() && !aiBusy,
                        backgroundColor = LightGlass,
                        textColor = Coal,
                        modifier = Modifier.weight(0.4f)
                    )
                    BrutalistButton(
                        text = "POST TO WALL",
                        onClick = {
                            if (caption.isNotBlank()) {
                                viewModel.post(
                                    caption = caption,
                                    imageUrl = "https://source.unsplash.com/800x600/?campus,event"
                                )
                                caption = ""
                            }
                        },
                        enabled = caption.isNotBlank(),
                        backgroundColor = Copper,
                        modifier = Modifier.weight(0.6f)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            items(posts) { post ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neoBrutalism(backgroundColor = Snow, shadowOffset = 8.dp)
                ) {
                    Column {
                        AsyncImage(
                            model = post.imageUrl.ifBlank { "https://source.unsplash.com/800x600/?event,moment" },
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .border(3.dp, color = Coal),
                            contentScale = ContentScale.Crop
                        )
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (post.isPinned) {
                                Box(
                                    modifier = Modifier
                                        .neoBrutalism(backgroundColor = Moss, shadowOffset = 2.dp)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "PINNED BY HOST",
                                        style = GatherTypography.labelSmall,
                                        color = Snow
                                    )
                                }
                            }
                            Text(
                                text = post.caption.uppercase(),
                                style = GatherTypography.titleLarge,
                                color = Coal
                            )
                        }
                    }
                }
            }
        }
    }
}