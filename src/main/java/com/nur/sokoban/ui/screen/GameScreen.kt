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
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    level: Level?,
    onNavigateToLevels: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    if (level == null) {
        return
    }


    var pX by remember { mutableIntStateOf(level.playerStartX) }
    var pY by remember { mutableIntStateOf(level.playerStartY) }
    var heroOnGoal by remember { mutableStateOf(level.playerOnGoalAtStart) }
    var showWinDialog by remember { mutableStateOf(false) }
    val levelD = remember { mutableStateListOf(*level.grid.toTypedArray()) }

    // For swipe
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    fun reset() {
        pX = level.playerStartX
        pY = level.playerStartY
        heroOnGoal = level.playerOnGoalAtStart
        levelD.clear()
        levelD.addAll(level.grid.toTypedArray())
    }

    fun move(dx: Int, dy: Int) {
        Log.d("Sokoban", "move called: pX=$pX, pY=$pY, dx=$dx, dy=$dy")
        val nextX = pX + dx
        val nextY = pY + dy
        if (nextX < 0 || nextX >= level.width || nextY < 0 || nextY >= level.height) return

        val nextIndex = nextY * level.width + nextX
        val target = levelD[nextIndex]

        if (target == 1) return

        if (target == 0 || target == 3) {
            levelD[pY * level.width + pX] = if (heroOnGoal) 3 else 0
            pX = nextX
            pY = nextY
            levelD[nextIndex] = 4
            heroOnGoal = (target == 3)
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
            }
        }

        if (2 !in levelD) {
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
                    Text("Level ${level.number}")
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
            GameLevelCanvas(
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(level.width.toFloat() / level.height.toFloat()),
                levelData = levelD.toList(),
                levelWidth = level.width,
                levelHeight = level.height
            )
        }

        if (showWinDialog) {
            AlertDialog(
                onDismissRequest = { showWinDialog = false },
                title = { Text("Congratulations!") },
                text = { Text("You have won!") },
                confirmButton = {
                    Button(onClick = {
                        reset()
                        showWinDialog = false
                    }) {
                        Text("Play Again")
                    }
                },
                dismissButton = {
                    Button(onClick = { showWinDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

@SuppressLint("LocalContextResourcesRead")
@Composable
fun GameLevelCanvas(
    modifier: Modifier = Modifier,
    levelData: List<Int>,
    levelWidth: Int,
    levelHeight: Int
) {
    val context = LocalContext.current

    val tiles = remember {
        arrayOf(
            BitmapFactory.decodeResource(context.resources, R.drawable.empty).asImageBitmap(), // 0
            BitmapFactory.decodeResource(context.resources, R.drawable.wall).asImageBitmap(),  // 1
            BitmapFactory.decodeResource(context.resources, R.drawable.box).asImageBitmap(),   // 2
            BitmapFactory.decodeResource(context.resources, R.drawable.goal).asImageBitmap(),  // 3
            BitmapFactory.decodeResource(context.resources, R.drawable.hero).asImageBitmap(),  // 4
            BitmapFactory.decodeResource(context.resources, R.drawable.boxok).asImageBitmap()  // 5
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val tileWidthPx = size.width / levelWidth
        val tileHeightPx = size.height / levelHeight
        val tileSize = minOf(tileWidthPx, tileHeightPx)

        for (y in 0 until levelHeight) {
            for (x in 0 until levelWidth) {
                val tileIndex = levelData[y * levelWidth + x]
                val img: ImageBitmap = tiles[tileIndex.coerceIn(0, tiles.lastIndex)]

                drawImage(
                    image = img,
                    dstOffset = IntOffset(
                        x = (x * tileSize).roundToInt(),
                        y = (y * tileSize).roundToInt()
                    ),
                    dstSize = IntSize(
                        width = tileSize.roundToInt(),
                        height = tileSize.roundToInt()
                    )
                )
            }
        }
    }
}
