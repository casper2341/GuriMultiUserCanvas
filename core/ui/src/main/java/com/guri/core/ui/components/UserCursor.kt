package com.guri.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopStart
    ) {
        Canvas(
            modifier = Modifier.size(16.dp)
        ) {
            // Draw cursor circle
            drawCircle(
                color = Color(cursorPosition.color.toULong()),
                radius = 6.dp.toPx(),
                center = Offset(8.dp.toPx(), 8.dp.toPx())
            )
            
            // Draw cursor pointer
            drawLine(
                color = Color(cursorPosition.color.toULong()),
                start = Offset(8.dp.toPx(), 8.dp.toPx()),
                end = Offset(16.dp.toPx(), 16.dp.toPx()),
                strokeWidth = 2.dp.toPx()
            )
        }
        
        // Show the user name
        Text(
            text = cursorPosition.userName.take(10),
            color = Color(cursorPosition.color.toULong()),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
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
            color = 0xFFFF0000,
            visible = true
        )
    )
} 