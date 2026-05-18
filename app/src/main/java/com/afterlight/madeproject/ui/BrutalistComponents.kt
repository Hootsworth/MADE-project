package com.afterlight.madeproject.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.afterlight.madeproject.ui.theme.*

@Composable
fun BrutalistButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Copper,
    textColor: Color = Snow,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .neoBrutalism(
                backgroundColor = if (enabled) backgroundColor else LightGlass,
                shadowOffset = if (enabled) 6.dp else 0.dp
            )
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Text(
            text = text.uppercase(),
            style = GatherTypography.labelLarge.copy(color = if (enabled) textColor else LightTextMuted),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun BrutalistTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = label.uppercase(), style = GatherTypography.labelMedium, color = Coal)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = GatherTypography.bodyLarge.copy(color = Coal),
            modifier = Modifier
                .fillMaxWidth()
                .let { if (singleLine) it.height(56.dp) else it.heightIn(min = 56.dp) }
                .neoBrutalism(backgroundColor = Snow, shadowOffset = 4.dp)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            singleLine = singleLine
        )
    }
}