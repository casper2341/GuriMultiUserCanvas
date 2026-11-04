package com.guri.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Rectangle
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.guri.core.model.DrawPath

/**
 * A tool selector component for the drawing tools
 */
@Composable
fun DrawingToolSelector(
    selectedTool: DrawPath.ToolType,
    onToolSelected: (DrawPath.ToolType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DrawPath.ToolType.values().forEach { tool ->
            ToolItem(
                tool = tool,
                isSelected = tool == selectedTool,
                onClick = { onToolSelected(tool) }
            )
        }
    }
}

@Composable
private fun ToolItem(
    tool: DrawPath.ToolType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = when (tool) {
        DrawPath.ToolType.PEN -> Icons.Filled.Create
        DrawPath.ToolType.MARKER -> Icons.Filled.FormatColorFill
        DrawPath.ToolType.ERASER -> Icons.Outlined.Clear
        DrawPath.ToolType.SHAPE_LINE -> Icons.Filled.HorizontalRule
        DrawPath.ToolType.SHAPE_RECTANGLE -> Icons.Filled.Rectangle
        DrawPath.ToolType.SHAPE_CIRCLE -> Icons.Filled.Circle
        DrawPath.ToolType.TEXT -> Icons.Outlined.TextFields
    }
    
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Tool ${tool.name}",
            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                   else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DrawingToolSelectorPreview() {
    DrawingToolSelector(
        selectedTool = DrawPath.ToolType.PEN,
        onToolSelected = {}
    )
} 