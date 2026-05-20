package com.afterlight.madeproject.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.neoBrutalism(
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    borderWidth: Dp = 2.5.dp,
    shadowColor: Color? = null,
    shadowOffset: Dp = 6.dp,
    cornerRadius: Dp = 0.dp // Keep it flat and sharp
) = this
    .let {
        val resolvedBackground = backgroundColor ?: MaterialTheme.colorScheme.surface
        val resolvedShadow = shadowColor ?: MaterialTheme.colorScheme.onSurface

        it
            .drawBehind {
                drawRoundRect(
                    color = resolvedShadow,
                    topLeft = Offset(shadowOffset.toPx(), shadowOffset.toPx()),
                    size = size,
                    cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
                )
            }
            .background(resolvedBackground, RoundedCornerShape(cornerRadius))
            .clip(RoundedCornerShape(cornerRadius))
    }