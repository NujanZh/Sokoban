package com.nur.sokoban

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nur.sokoban.data.model.Level
import com.nur.sokoban.ui.screen.GameScreen
import com.nur.sokoban.ui.screen.LevelScreen
import com.nur.sokoban.ui.screen.SettingScreen
import com.nur.sokoban.ui.theme.SokobanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SokobanTheme {
                val navController = rememberNavController()
                var selectedLevel by remember {mutableStateOf<Level?>(null)}

                NavHost(
                    navController = navController,
                    startDestination = "levels"
                ) {
                    composable("levels") {
                        LevelScreen(
                            onLevelSelected = { level ->
                                selectedLevel = level
                                navController.navigate("game")
                            },
                            navigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable("game") {
                        GameScreen(
                            level = selectedLevel,
                            onNavigateToLevels = {
                                navController.navigate("levels") {
                                    popUpTo("levels") { inclusive = true }
                                }
                            },
                            onNavigateToSettings = {
                                navController.navigate("settings")
                            }
                        )
                    }
                    composable("settings") {
                        SettingScreen(
                            navigateBack = {navController.popBackStack()}
                        )
                    }
                }
            }
        }
    }
}
