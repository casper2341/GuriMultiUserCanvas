package com.guri.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.guri.core.model.Participant

/**
 * Displays the participants of a room, with an admin-only kick affordance.
 * Reused both in the room lobby and in the in-canvas participants sheet.
 */
@Composable
fun ParticipantsList(
    participants: List<Participant>,
    currentUserId: String,
    isCurrentUserAdmin: Boolean,
    onKick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(participants, key = { it.userId }) { participant ->
            ParticipantRow(
                participant = participant,
                canKick = isCurrentUserAdmin && participant.userId != currentUserId,
                onKick = { onKick(participant.userId) }
            )
        }
    }
}

@Composable
private fun ParticipantRow(
    participant: Participant,
    canKick: Boolean,
    onKick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val avatarColor = Color(participant.color.takeIf { it != -1 } ?: 0xFF808080.toInt())

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(avatarColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = participant.userName.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = participant.userName,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!participant.isActive) {
                        Text(
                            text = "Offline",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                if (participant.isAdmin) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Admin",
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(16.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            if (canKick) {
                IconButton(onClick = onKick) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Kick ${participant.userName}",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ParticipantsListPreview() {
    ParticipantsList(
        participants = listOf(
            Participant(userId = "1", userName = "Alex", isAdmin = true, color = 0xFFFF0000.toInt()),
            Participant(userId = "2", userName = "Sam", color = 0xFF0000FF.toInt())
        ),
        currentUserId = "1",
        isCurrentUserAdmin = true,
        onKick = {}
    )
}
