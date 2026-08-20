package com.guri.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.guri.core.common.AppConstants

/**
 * A brush size selector component for the drawing tools
 */
@Composable
fun BrushSizeSelector(
    sizes: List<Float>,
    selectedSize: Float,
    onSizeSelected: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        sizes.forEach { size ->
            BrushSizeItem(
                size = size,
                isSelected = size == selectedSize,
                onClick = { onSizeSelected(size) }
            )
        }
    }
}

@Composable
private fun BrushSizeItem(
    size: Float,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sizeDp = (size * 1.5f).coerceAtMost(24f).dp
    
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(sizeDp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BrushSizeSelectorPreview() {
    BrushSizeSelector(
        sizes = AppConstants.BRUSH_SIZES,
        selectedSize = AppConstants.BRUSH_SIZES[1],
        onSizeSelected = {}
    )
} 