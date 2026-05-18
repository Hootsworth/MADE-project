package com.afterlight.madeproject.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.neoBrutalism(
    backgroundColor: Color = Pearl,
    borderColor: Color = Coal,
    borderWidth: Dp = 2.5.dp,
    shadowColor: Color = Coal,
    shadowOffset: Dp = 6.dp,
    cornerRadius: Dp = 0.dp // Keep it flat and sharp
) = this
    .drawBehind {
        drawRoundRect(
            color = shadowColor,
            topLeft = Offset(shadowOffset.toPx(), shadowOffset.toPx()),
            size = size,
            cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
        )
    }
    .border(borderWidth, borderColor, RoundedCornerShape(cornerRadius))
    .background(backgroundColor, RoundedCornerShape(cornerRadius))
    .clip(RoundedCornerShape(cornerRadius))