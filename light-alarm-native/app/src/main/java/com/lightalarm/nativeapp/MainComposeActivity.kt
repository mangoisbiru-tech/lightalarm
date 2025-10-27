package com.lightalarm.nativeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lightalarm.nativeapp.data.LightTheme
import com.lightalarm.nativeapp.ui.screens.*
import com.lightalarm.nativeapp.ui.theme.LightAlarmTheme
import com.lightalarm.nativeapp.viewmodel.AlarmViewModel
import com.lightalarm.nativeapp.viewmodel.AlarmViewModelFactory

class MainComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Optional: Enable edge-to-edge for immersive UI (API 29+)
        // WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            LightAlarmTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LightAlarmApp()
                }
            }
        }
    }
}

@Composable
fun LightAlarmApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val viewModel: AlarmViewModel = viewModel(
        factory = AlarmViewModelFactory(context.applicationContext as android.app.Application)
    )
    
    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainScreenCompose(
                onNavigateToAddAlarm = { navController.navigate("add_alarm") },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToPreview = { 
                    // Pass selected theme from ViewModel for dynamic preview
                    navController.navigate("preview")
                },
                viewModel = viewModel
            )
        }
        
        composable("add_alarm") {
            AddAlarmScreenCompose(
                onNavigateBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }
        
        composable("preview") {
            WakeUpPreviewScreen(
                theme = LightTheme.SUNRISE,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable("settings") {
            // Implement your settings screen here
            SettingsScreenCompose(
                onNavigateBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }
    }
}
