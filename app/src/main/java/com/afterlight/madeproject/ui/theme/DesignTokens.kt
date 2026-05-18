package com.afterlight.madeproject.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

@Immutable
data class GatherShapes(
    val card: RoundedCornerShape = RoundedCornerShape(0.dp), // Hard edges for that classic 2D feel
    val pill: RoundedCornerShape = RoundedCornerShape(999.dp)
)

@Immutable
data class GatherSpacing(
    val xs: Int = 4,
    val s: Int = 8,
    val m: Int = 16,
    val l: Int = 24,
    val xl: Int = 32,
    val xxl: Int = 48
)

val LocalGatherShapes = staticCompositionLocalOf { GatherShapes() }