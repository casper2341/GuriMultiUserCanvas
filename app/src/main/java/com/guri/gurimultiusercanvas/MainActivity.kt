package com.guri.gurimultiusercanvas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.guri.feature.auth.SignInScreen
import com.guri.feature.canvas.CanvasScreen
import com.guri.feature.room.create.CreateRoomScreen
import com.guri.feature.room.join.JoinRoomScreen
import com.guri.feature.room.lobby.RoomLobbyScreen
import com.guri.gurimultiusercanvas.ui.theme.GuriMultiUserCanvasTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GuriMultiUserCanvasTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavHost(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val startDestination = if (FirebaseAuth.getInstance().currentUser != null) "home" else "sign_in"

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(route = "sign_in") {
            SignInScreen(
                onSignedIn = {
                    navController.navigate("home") {
                        popUpTo("sign_in") { inclusive = true }
                    }
                }
            )
        }

        composable(route = "home") {
            HomeScreen(
                onCreateRoom = { navController.navigate("create_room") },
                onJoinRoom = { navController.navigate("join_room") }
            )
        }

        composable(route = "create_room") {
            CreateRoomScreen(
                onRoomCreated = { roomId -> navController.navigate("room_lobby/$roomId") },
                onBack = { navController.popBackStack() }
            )
        }

        composable(route = "join_room") {
            JoinRoomScreen(
                onRoomJoined = { roomId -> navController.navigate("room_lobby/$roomId") },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "room_lobby/{roomId}",
            arguments = listOf(navArgument("roomId") { type = NavType.StringType })
        ) {
            RoomLobbyScreen(
                onEnterCanvas = { roomId ->
                    navController.navigate("canvas/$roomId") {
                        popUpTo("room_lobby/{roomId}") { inclusive = true }
                    }
                },
                onLeaveRoom = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = "canvas/{roomId}",
            arguments = listOf(navArgument("roomId") { type = NavType.StringType })
        ) {
            CanvasScreen(
                onLeaveRoom = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

@Composable
fun HomeScreen(
    onCreateRoom: () -> Unit,
    onJoinRoom: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Soft decorative color blobs for a playful, poster-like backdrop.
        Box(
            modifier = Modifier
                .offset(x = (-60).dp, y = (-40).dp)
                .size(220.dp)
                .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f), CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 70.dp, y = 70.dp)
                .size(260.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Guri Canvas",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Draw together, live, with friends and family!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onCreateRoom,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Icon(imageVector = Icons.Filled.Palette, contentDescription = null)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Create a Room", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = onJoinRoom,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.secondary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Icon(imageVector = Icons.Filled.Groups, contentDescription = null)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Join a Room", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GuriMultiUserCanvasTheme {
        HomeScreen(
            onCreateRoom = {},
            onJoinRoom = {}
        )
    }
}
