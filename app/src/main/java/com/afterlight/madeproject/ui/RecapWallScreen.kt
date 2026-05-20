package com.afterlight.madeproject.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.afterlight.madeproject.ui.components.SmoothButton
import com.afterlight.madeproject.ui.components.SmoothTextField
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
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(top = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Recap Wall", style = GatherTypography.displayLarge, color = Coal)
            Text(text = "Event Documentation", style = GatherTypography.bodyLarge, color = LightTextMuted)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Pearl),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text(
                    text = "Log Moment",
                    style = GatherTypography.titleLarge,
                    color = Coal
                )

                SmoothTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = "Add archive note"
                )

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    SmoothButton(
                        text = if (aiBusy) "Polishing..." else "AI Polish",
                        onClick = { viewModel.polishCaption(caption) { caption = it } },
                        enabled = caption.isNotBlank() && !aiBusy,
                        containerColor = Pearl.copy(alpha = 0.5f),
                        contentColor = Coal,
                        modifier = Modifier.weight(0.4f)
                    )
                    SmoothButton(
                        text = "Post to Wall",
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
                        containerColor = Copper,
                        contentColor = Snow,
                        modifier = Modifier.weight(0.6f)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(posts) { post ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Pearl),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        AsyncImage(
                            model = post.imageUrl.ifBlank { "https://source.unsplash.com/800x600/?event,moment" },
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            contentScale = ContentScale.Crop
                        )
                        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (post.isPinned) {
                                Surface(
                                    shape = CircleShape,
                                    color = Moss.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "Pinned by Host",
                                        style = GatherTypography.labelMedium,
                                        color = Moss,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                            Text(
                                text = post.caption,
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