package com.guri.feature.canvas

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.guri.core.common.AppConstants
import com.guri.core.ui.components.BrushSizeSelector
import com.guri.core.ui.components.ColorSelector
import com.guri.core.ui.components.DrawingCanvas
import com.guri.core.ui.components.DrawingToolSelector
import com.guri.core.ui.components.ParticipantsList
import com.guri.core.ui.components.UserCursor
import com.guri.feature.canvas.chat.ChatPanel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanvasScreen(
    onLeaveRoom: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CanvasViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showParticipants by remember { mutableStateOf(false) }
    var showChat by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.wasKickedOrEnded) {
        if (uiState.wasKickedOrEnded) {
            onLeaveRoom()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeError()
        }
    }

    LaunchedEffect(uiState.savedImageUrl) {
        uiState.savedImageUrl?.let {
            snackbarHostState.showSnackbar("Canvas saved")
            viewModel.consumeSavedImageUrl()
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        uiState.room?.name ?: "Canvas",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.leaveRoom()
                        onLeaveRoom()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Leave room")
                    }
                },
                actions = {
                    IconButton(onClick = { showChat = !showChat }) {
                        Icon(
                            Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "Chat",
                            tint = if (showChat) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { showParticipants = true }) {
                        Icon(
                            Icons.Filled.People,
                            contentDescription = "Participants",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = viewModel::saveCanvasImage) {
                        Icon(
                            Icons.Filled.Save,
                            contentDescription = "Save canvas",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Floating rounded "dock" holding every drawing control.
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DrawingToolSelector(selectedTool = uiState.currentTool, onToolSelected = viewModel::selectTool)
                    Spacer(modifier = Modifier.width(14.dp))
                    ColorSelector(
                        colors = AppConstants.DEFAULT_COLORS,
                        selectedColor = uiState.currentColor,
                        onColorSelected = viewModel::selectColor
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    BrushSizeSelector(
                        sizes = AppConstants.BRUSH_SIZES,
                        selectedSize = uiState.currentStrokeWidth,
                        onSizeSelected = viewModel::selectStrokeWidth
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    IconButton(
                        onClick = viewModel::undo,
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
                    }
                    IconButton(
                        onClick = viewModel::redo,
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo")
                    }
                    IconButton(
                        onClick = viewModel::requestClearCanvas,
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear canvas")
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                // The drawing surface sits like a page on the colorful background.
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        DrawingCanvas(
                            paths = uiState.paths,
                            currentColor = uiState.currentColor,
                            currentStrokeWidth = uiState.currentStrokeWidth,
                            currentTool = uiState.currentTool,
                            onPathDrawn = viewModel::onPathDrawn,
                            onCursorPositionChanged = viewModel::onCursorMoved,
                            onTextRequested = viewModel::onTextRequested,
                            isLocked = uiState.isLocked && !uiState.isAdmin,
                            modifier = Modifier
                                .fillMaxSize()
                                .onSizeChanged { viewModel.onCanvasSizeChanged(it) }
                        )

                        uiState.cursorPositions.forEach { cursor ->
                            UserCursor(
                                cursorPosition = cursor,
                                modifier = Modifier.offset {
                                    IntOffset(cursor.x.roundToInt(), cursor.y.roundToInt())
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (showChat) {
                Card(
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    ChatPanel(
                        messages = uiState.chatMessages,
                        currentUserId = uiState.currentUserId,
                        onSendMessage = viewModel::sendChatMessage,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    uiState.pendingTextOffset?.let {
        var textValue by remember(it) { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = viewModel::onTextCancelled,
            shape = RoundedCornerShape(28.dp),
            confirmButton = {
                TextButton(onClick = { viewModel.onTextConfirmed(textValue) }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onTextCancelled) { Text("Cancel") }
            },
            title = { Text("Add text") },
            text = {
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
            }
        )
    }

    if (uiState.showClearConfirmation) {
        AlertDialog(
            onDismissRequest = viewModel::cancelClearCanvas,
            shape = RoundedCornerShape(28.dp),
            confirmButton = {
                TextButton(
                    onClick = viewModel::confirmClearCanvas,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelClearCanvas) { Text("Cancel") }
            },
            title = { Text("Clear canvas?") },
            text = { Text("This removes every stroke for everyone in the room. This can't be undone.") }
        )
    }

    if (showParticipants) {
        ModalBottomSheet(onDismissRequest = { showParticipants = false }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Participants (${uiState.participants.size}/" +
                        "${uiState.room?.maxParticipants ?: AppConstants.MAX_ROOM_PARTICIPANTS})",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (uiState.isAdmin) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Lock room", style = MaterialTheme.typography.titleMedium)
                        Switch(checked = uiState.isLocked, onCheckedChange = { viewModel.toggleLock() })
                    }
                }

                ParticipantsList(
                    participants = uiState.participants,
                    currentUserId = uiState.currentUserId,
                    isCurrentUserAdmin = uiState.isAdmin,
                    onKick = viewModel::kick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
