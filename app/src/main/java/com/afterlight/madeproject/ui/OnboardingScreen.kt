package com.afterlight.madeproject.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.afterlight.madeproject.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pages = listOf(
        OnboardingPage(
            title = "Find the right event fast.",
            subtitle = "Minimal feed, strong signal.",
            image = "https://images.unsplash.com/photo-1543269865-cbf427effbad?q=80&w=2070&auto=format&fit=crop",
            color = Sand
        ),
        OnboardingPage(
            title = "Use clear filters, not clutter.",
            subtitle = "Less scroll, better choices.",
            image = "https://images.unsplash.com/photo-1523580494863-6f3031224c94?q=80&w=2070&auto=format&fit=crop",
            color = Moss
        ),
        OnboardingPage(
            title = "RSVP and move on.",
            subtitle = "Clean campus flow.",
            image = "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?q=80&w=2070&auto=format&fit=crop",
            color = Copper
        )
    )

    val pagerState = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Snow)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { pageIndex ->
            val page = pages[pageIndex]

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // High-Contrast Framed Image Area
                Box(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, top = 48.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Coal.copy(alpha = 0.15f)),
                        color = Pearl
                    ) {
                        AsyncImage(
                            model = page.image,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Brutalist Color Tag anchored to the image
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = page.color,
                        border = BorderStroke(1.dp, Coal.copy(alpha = 0.1f))
                    ) {
                        Text(
                            text = "ISSUE 0${pageIndex + 1}",
                            style = GatherTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Coal,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                // Stark Editorial Typography Area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "[ 0${pageIndex + 1} / 0${pages.size} ]",
                        style = GatherTypography.labelMedium,
                        color = Coal.copy(alpha = 0.4f)
                    )

                    Text(
                        text = page.title,
                        style = GatherTypography.displayLarge.copy(
                            fontSize = 40.sp,
                            lineHeight = 44.sp,
                            color = Coal
                        )
                    )

                    Text(
                        text = page.subtitle,
                        style = GatherTypography.bodyLarge.copy(
                            color = LightTextMuted
                        )
                    )
                }
            }
        }

        // Geometric Bottom Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Segmented Progress Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(pages.size) { index ->
                    val isSelected = index <= pagerState.currentPage
                    val weight = if (index == pagerState.currentPage) 2f else 1f

                    Box(
                        modifier = Modifier
                            .weight(weight)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(99.dp))
                            .background(if (isSelected) Coal else Pearl)
                    )
                }
            }

            // High-Contrast Action Block
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (pagerState.currentPage < pages.size - 1) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clickable(onClick = onFinish),
                        shape = RoundedCornerShape(8.dp),
                        color = Snow,
                        border = BorderStroke(1.dp, Coal.copy(alpha = 0.12f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("Skip", style = GatherTypography.labelLarge, color = Coal)
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clickable {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                        shape = RoundedCornerShape(8.dp),
                        color = Coal,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("Next", style = GatherTypography.labelLarge, color = Snow)
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clickable(onClick = onFinish),
                        shape = RoundedCornerShape(8.dp),
                        color = Coal,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("Initialize", style = GatherTypography.labelLarge, color = Snow)
                        }
                    }
                }
            }
        }
    }
}

data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val image: String,
    val color: Color
)