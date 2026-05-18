package com.afterlight.madeproject.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.afterlight.madeproject.ui.components.BrutalistButton
import com.afterlight.madeproject.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pages = listOf(
        OnboardingPage(
            title = "FIND THE RIGHT EVENT FAST.",
            subtitle = "MINIMAL FEED, STRONG SIGNAL",
            image = "https://images.unsplash.com/photo-1543269865-cbf427effbad?q=80&w=2070&auto=format&fit=crop",
            color = Sand
        ),
        OnboardingPage(
            title = "USE CLEAR FILTERS, NOT CLUTTER.",
            subtitle = "LESS SCROLL, BETTER CHOICES",
            image = "https://images.unsplash.com/photo-1523580494863-6f3031224c94?q=80&w=2070&auto=format&fit=crop",
            color = Moss
        ),
        OnboardingPage(
            title = "RSVP AND MOVE ON.",
            subtitle = "CLEAN CAMPUS FLOW",
            image = "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?q=80&w=2070&auto=format&fit=crop",
            color = Copper
        )
    )
    
    val pagerState = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Snow)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            val page = pages[pageIndex]
            
            Box(modifier = Modifier.fillMaxSize()) {
                // Background Image
                AsyncImage(
                    model = page.image,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Coal.copy(alpha = 0.2f),
                                    Coal.copy(alpha = 0.9f)
                                )
                            )
                        )
                )
                
                // Content
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 32.dp, vertical = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .neoBrutalism(backgroundColor = page.color, shadowOffset = 4.dp)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "ISSUE 0${pageIndex + 1}",
                            style = GatherTypography.labelLarge,
                            color = if (page.color == Coal) Snow else Coal
                        )
                    }
                    
                    Text(
                        text = page.title,
                        style = GatherTypography.displayLarge.copy(
                            fontSize = 42.sp,
                            lineHeight = 48.sp,
                            color = Snow
                        )
                    )
                    
                    Text(
                        text = page.subtitle,
                        style = GatherTypography.labelMedium.copy(
                            color = Sand,
                            letterSpacing = 2.sp
                        )
                    )
                }
            }
        }

        // Bottom Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Page Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { index ->
                    val isSelected = index == pagerState.currentPage
                    val width by animateDpAsState(targetValue = if (isSelected) 48.dp else 12.dp, label = "")
                    
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(width, 4.dp)
                            .background(if (isSelected) Snow else Snow.copy(alpha = 0.3f))
                    )
                }
            }
            
            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (pagerState.currentPage < pages.size - 1) {
                    BrutalistButton(
                        text = "SKIP",
                        onClick = onFinish,
                        backgroundColor = Color.Transparent,
                        textColor = Snow,
                        modifier = Modifier
                            .weight(1f)
                            .border(2.dp, Snow)
                    )
                    BrutalistButton(
                        text = "NEXT",
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        backgroundColor = Snow,
                        textColor = Coal,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    BrutalistButton(
                        text = "INITIALIZE APP",
                        onClick = onFinish,
                        backgroundColor = Sand,
                        textColor = Coal,
                        modifier = Modifier.fillMaxWidth()
                    )
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