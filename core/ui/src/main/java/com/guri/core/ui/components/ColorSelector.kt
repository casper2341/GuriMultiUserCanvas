package com.guri.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.guri.core.common.AppConstants

/**
 * A color selector component for the drawing tools
 */
@Composable
fun ColorSelector(
    colors: List<Long>,
    selectedColor: Long,
    onColorSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        colors.forEach { color ->
            ColorItem(
                color = color,
                isSelected = color == selectedColor,
                onClick = { onColorSelected(color) }
            )
        }
    }
}

@Composable
private fun ColorItem(
    color: Long,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderWidth = if (isSelected) 3.dp else 1.dp
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Color(color.toULong()))
            .border(borderWidth, borderColor, CircleShape)
            .clickable(onClick = onClick)
            .padding(4.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun ColorSelectorPreview() {
    ColorSelector(
        colors = AppConstants.DEFAULT_COLORS.map { it },
        selectedColor = AppConstants.DEFAULT_COLORS[0],
        onColorSelected = {}
    )
} 