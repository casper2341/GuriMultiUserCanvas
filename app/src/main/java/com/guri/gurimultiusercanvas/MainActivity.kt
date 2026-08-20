package com.guri.gurimultiusercanvas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
    
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        composable(route = "home") {
            HomeScreen(
                onCreateRoom = { navController.navigate("create_room") },
                onJoinRoom = { navController.navigate("join_room") }
            )
        }
        
        // We'll add more routes as we implement the features
        composable(route = "create_room") {
            TemporaryScreen(text = "Create Room Screen")
        }
        
        composable(route = "join_room") {
            TemporaryScreen(text = "Join Room Screen")
        }
    }
}

@Composable
fun HomeScreen(
    onCreateRoom: () -> Unit,
    onJoinRoom: () -> Unit,
    modifier: Modifier = Modifier
) {
    // We'll replace this with a proper HomeScreen later
    TemporaryScreen(text = "Welcome to Multi-User Canvas!")
}

@Composable
fun TemporaryScreen(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier
    )
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