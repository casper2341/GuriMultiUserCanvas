package com.guri.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
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
        horizontalArrangement = Arrangement.spacedBy(10.dp),
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
    val swatchColor = Color(color)
    val isLight = relativeLuminance(swatchColor) > 0.7f
    val ringColor = MaterialTheme.colorScheme.primary
    val size = if (isSelected) 40.dp else 34.dp

    Box(
        modifier = modifier
            .size(44.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(swatchColor)
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) ringColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    shape = CircleShape
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Selected",
                    tint = if (isLight) Color.Black.copy(alpha = 0.6f) else Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun relativeLuminance(color: Color): Float =
    (0.299f * color.red + 0.587f * color.green + 0.114f * color.blue)

@Preview(showBackground = true)
@Composable
private fun ColorSelectorPreview() {
    ColorSelector(
        colors = AppConstants.DEFAULT_COLORS.map { it },
        selectedColor = AppConstants.DEFAULT_COLORS[0],
        onColorSelected = {}
    )
}