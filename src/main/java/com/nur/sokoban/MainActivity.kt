package com.nur.sokoban

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nur.sokoban.ui.screen.GameScreen
import com.nur.sokoban.ui.screen.SettingScreen
import com.nur.sokoban.ui.theme.SokobanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SokobanTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "game"
                ) {
                    composable("game") {
                        GameScreen(navController)
                    }
                    composable("settings") {
                        SettingScreen(navigateBack = {navController.popBackStack()})
                    }
                }
            }
        }
    }
}
