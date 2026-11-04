package com.guri.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guri.core.model.CursorPosition

/**
 * A component that shows another user's cursor position
 */
@Composable
fun UserCursor(
    cursorPosition: CursorPosition,
    modifier: Modifier = Modifier
) {
    if (!cursorPosition.visible) return

    val color = Color(cursorPosition.color)

    Box(
        modifier = modifier.wrapContentSize(unbounded = true),
        contentAlignment = Alignment.TopStart
    ) {
        Canvas(
            modifier = Modifier.size(18.dp)
        ) {
            // Draw cursor circle with a white halo so it stays visible on any stroke color
            drawCircle(color = Color.White, radius = 8.dp.toPx(), center = Offset(8.dp.toPx(), 8.dp.toPx()))
            drawCircle(color = color, radius = 6.dp.toPx(), center = Offset(8.dp.toPx(), 8.dp.toPx()))

            // Draw cursor pointer
            drawLine(
                color = color,
                start = Offset(8.dp.toPx(), 8.dp.toPx()),
                end = Offset(18.dp.toPx(), 18.dp.toPx()),
                strokeWidth = 2.5.dp.toPx()
            )
        }

        // Name chip — a small colored pill reads far better on a busy drawing than bare text
        Box(
            modifier = Modifier
                .padding(top = 22.dp)
                .clip(RoundedCornerShape(50))
                .background(color)
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = cursorPosition.userName.take(10),
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserCursorPreview() {
    UserCursor(
        cursorPosition = CursorPosition(
            roomId = "room1",
            userId = "user1",
            userName = "User 1",
            x = 100f,
            y = 100f,
            color = 0xFFFF0000.toInt(),
            visible = true
        )
    )
} 