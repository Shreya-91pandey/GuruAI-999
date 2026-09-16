package com.guruai.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.guruai.app.ui.screens.ChatScreen
import com.guruai.app.ui.screens.SettingsScreen
import com.guruai.app.ui.theme.GuruAITheme
import com.guruai.app.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GuruAITheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    GuruAIApp()
                }
            }
        }
    }
}

@Composable
fun GuruAIApp() {
    val navController = rememberNavController()
    val chatViewModel: ChatViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "chat"
    ) {
        composable("chat") {
            ChatScreen(
                onOpenSettings = { navController.navigate("settings") },
                viewModel = chatViewModel
            )
        }
        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onKeysSaved = {
                    chatViewModel.refreshStatus()
                }
            )
        }
    }
}
