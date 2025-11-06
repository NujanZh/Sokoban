package com.nur.sokoban.ui.screen

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.nur.sokoban.R
import com.nur.sokoban.data.model.Level
import com.nur.sokoban.data.model.LevelState
import com.nur.sokoban.data.model.ProgressManager
import com.nur.sokoban.ui.LevelCanvas
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    level: Level?,
    onNavigateToLevels: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    if (level == null) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = { Text("Sokoban") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateToLevels) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Levels Menu"
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("Choose level", style = MaterialTheme.typography.headlineMedium)
            }
        }
        return
    }

    val context = LocalContext.current
    val savedState = remember(level) { ProgressManager.loadLevelState(context, level.number) }

    var pX by remember(level) { mutableIntStateOf(savedState?.playerX ?: level.playerStartX) }
    var pY by remember(level) { mutableIntStateOf(savedState?.playerY ?: level.playerStartY) }
    var heroOnGoal by remember(level) { mutableStateOf(savedState?.heroOnGoal ?: level.playerOnGoalAtStart) }
    var showWinDialog by remember { mutableStateOf(false) }
    var moveCount by remember(level) { mutableIntStateOf(savedState?.moveCount ?: 0) }
    val levelD = remember(level) {
        mutableStateListOf(*(savedState?.grid ?: level.grid).toTypedArray())
    }

    // For swipe
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    DisposableEffect(level) {
        onDispose {
            if (!showWinDialog) {
                val state = LevelState(
                    levelNumber = level.number,
                    grid = levelD.toIntArray(),
                    playerX = pX,
                    playerY = pY,
                    heroOnGoal = heroOnGoal,
                    moveCount = moveCount
                )
                ProgressManager.saveLevelState(context, state)
            }
        }
    }

    fun reset() {
        pX = level.playerStartX
        pY = level.playerStartY
        heroOnGoal = level.playerOnGoalAtStart
        moveCount = 0
        levelD.clear()
        levelD.addAll(level.grid.toTypedArray())
        ProgressManager.clearLevelState(context, level.number)
    }

    fun move(dx: Int, dy: Int) {
        val nextX = pX + dx
        val nextY = pY + dy
        if (nextX < 0 || nextX >= level.width || nextY < 0 || nextY >= level.height) return

        val nextIndex = nextY * level.width + nextX
        val target = levelD[nextIndex]

        if (target == 1) return

        var moved = false

        if (target == 0 || target == 3) {
            levelD[pY * level.width + pX] = if (heroOnGoal) 3 else 0
            pX = nextX
            pY = nextY
            levelD[nextIndex] = 4
            heroOnGoal = (target == 3)
            moved = true
        } else if (target == 2 || target == 5) {
            val beyondX = nextX + dx
            val beyondY = nextY + dy
            if (beyondX < 0 || beyondX >= level.width || beyondY < 0 || beyondY >= level.height) return

            val beyondIndex = beyondY * level.width + beyondX
            val beyond = levelD[beyondIndex]

            if (beyond == 0 || beyond == 3) {
                levelD[pY * level.width + pX] = if (heroOnGoal) 3 else 0
                pX = nextX
                pY = nextY
                levelD[nextIndex] = 4
                heroOnGoal = (target == 5)
                levelD[beyondIndex] = if (beyond == 0) 2 else 5
                moved = true
            }
        }

        if (moved) {
            moveCount++
        }

        if (2 !in levelD) {
            ProgressManager.saveLevelScore(context, level.number, moveCount)
            showWinDialog = true
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("Level ${level.number} - Moves: $moveCount")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateToLevels) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Levels Menu"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { reset() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset level",
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings Icon"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()

                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        },
                        onDragEnd = {
                            val threshold = 100f

                            when {
                                offsetX > threshold -> move(1, 0)
                                offsetX < -threshold -> move(-1, 0)
                                offsetY > threshold -> move(0, 1)
                                offsetY < -threshold -> move(0, -1)
                            }

                            offsetX = 0f
                            offsetY = 0f
                        }
                    )
                }
        ) {
            LevelCanvas(
                levelData = levelD.toList(),
                levelWidth = level.width,
                levelHeight = level.height,
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(level.width.toFloat() / level.height.toFloat())
            )
        }

        if (showWinDialog) {
            val bestScore = level.bestScore
            val isNewRecord = bestScore == null || moveCount < bestScore

            AlertDialog(
                onDismissRequest = { showWinDialog = false },
                title = { Text("Congratulations!") },
                text = {
                    Text(
                        if (isNewRecord) {
                            "You've completed the level with $moveCount moves! New record!"
                        } else {
                            "You've completed the level with"
                        }
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        reset()
                        showWinDialog = false
                    }) {
                        Text("Play Again")
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        showWinDialog = false
                        onNavigateToLevels()
                    }) {
                        Text("Other Levels")
                    }
                }
            )
        }
    }
}