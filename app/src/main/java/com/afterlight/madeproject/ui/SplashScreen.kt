package com.afterlight.madeproject.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.afterlight.madeproject.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onDone: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1400)
        onDone()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Coal),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .neoBrutalism(backgroundColor = Snow, borderColor = Copper, borderWidth = 4.dp, shadowOffset = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 120.dp, height = 12.dp)
                        .background(Copper)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "PAPERLIKE.",
                        style = GatherTypography.displayLarge,
                        color = Coal
                    )
                    Text(
                        text = "EDITION 2026",
                        style = GatherTypography.labelLarge,
                        color = Moss
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(Coal)
                )

                Text(
                    text = "CAMPUS ARCHIVE & EVENTS",
                    style = GatherTypography.labelMedium,
                    color = Coal
                )
            }
        }
    }
}