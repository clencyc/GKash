package com.example.g_kash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.g_kash.ui.theme.GKashTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import com.example.g_kash.navigation.Destination
import com.example.g_kash.navigation.AppNavHost
import androidx.compose.runtime.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GKashTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GKashApp()
                }
            }
        }
    }
}

@Composable
fun GKashApp() {
    val navController = rememberNavController()

    var isLoggedIn by remember { mutableStateOf(false) }
    var userId by remember { mutableStateOf<String?>(null) }

    val startDestination = if (isLoggedIn) {
        Destination.Home.route
    } else {
        Destination.Login.route
    }

    AppNavHost()
}