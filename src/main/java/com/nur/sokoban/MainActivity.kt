package com.nur.sokoban

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.nur.sokoban.ui.theme.SokobanTheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SokobanTheme {
                var resetTrigger by remember { mutableIntStateOf(0) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.primary,
                            ),
                            title = {
                                Text("Level 1")
                            },
                            actions = {
                                IconButton(onClick = {
                                    resetTrigger++
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Reset level",
                                    )
                                }
                            }
                        )
                    },
                ) { innerPadding ->
                    GameScreen(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        resetTrigger
                    )
                }
            }
        }
    }
}

private const val LEVEL_WIDTH = 10
private const val LEVEL_HEIGHT = 10

// Data levelu
private val levelData = intArrayOf(
    1, 1, 1, 1, 1, 1, 1, 1, 1, 0,
    1, 0, 0, 0, 0, 0, 0, 0, 1, 0,
    1, 0, 2, 3, 3, 2, 1, 0, 1, 0,
    1, 0, 1, 3, 2, 3, 2, 0, 1, 0,
    1, 0, 2, 3, 3, 2, 4, 0, 1, 0,
    1, 0, 1, 3, 2, 3, 2, 0, 1, 0,
    1, 0, 2, 3, 3, 2, 1, 0, 1, 0,
    1, 0, 0, 0, 0, 0, 0, 0, 1, 0,
    1, 1, 1, 1, 1, 1, 1, 1, 1, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0
)

@Composable
fun GameScreen(modifier: Modifier = Modifier, resetTrigger: Int = 0) {
    var pX by remember(resetTrigger) { mutableIntStateOf(6) }
    var pY by remember(resetTrigger) { mutableIntStateOf(4) }
    var heroOnGoal by remember(resetTrigger) { mutableStateOf(false) }
    var showWinDialog by remember { mutableStateOf(false) }
    val levelD = remember(resetTrigger) { mutableStateListOf(*levelData.toTypedArray()) }

    // For swipe
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val initialPX = 6
    val initialPY = 4


    fun reset() {
        pX = initialPX
        pY = initialPY
        heroOnGoal = false
        levelD.clear()
        levelD.addAll(levelData.toTypedArray())
    }

    fun move(dx: Int, dy: Int) {
        Log.d("Sokoban", "move called: pX=$pX, pY=$pY, dx=$dx, dy=$dy")
        val nextX = pX + dx
        val nextY = pY + dy
        if (nextX < 0 || nextX >= LEVEL_WIDTH || nextY < 0 || nextY >= LEVEL_HEIGHT) return

        val nextIndex = nextY * LEVEL_WIDTH + nextX
        val target = levelD[nextIndex]

        if (target == 1) return

        if (target == 0 || target == 3) {
            levelD[pY * LEVEL_WIDTH + pX] = if (heroOnGoal) 3 else 0
            pX = nextX
            pY = nextY
            levelD[nextIndex] = 4
            heroOnGoal = (target == 3)
        } else if (target == 2 || target == 5) {
            val beyondX = nextX + dx
            val beyondY = nextY + dy
            if (beyondX < 0 || beyondX >= LEVEL_WIDTH || beyondY < 0 || beyondY >= LEVEL_HEIGHT) return

            val beyondIndex = beyondY * LEVEL_WIDTH + beyondX
            val beyond = levelD[beyondIndex]

            if (beyond == 0 || beyond == 3) {
                levelD[pY * LEVEL_WIDTH + pX] = if (heroOnGoal) 3 else 0
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


    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(resetTrigger) {
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
            levelData = levelD.toList(),
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(LEVEL_WIDTH.toFloat() / LEVEL_HEIGHT.toFloat())
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

@Composable
fun GameLevelCanvas(levelData: List<Int>, modifier: Modifier = Modifier) {
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
        val tileWidthPx = size.width / LEVEL_WIDTH
        val tileHeightPx = size.height / LEVEL_HEIGHT
        val tileSize = minOf(tileWidthPx, tileHeightPx)

        for (y in 0 until LEVEL_HEIGHT) {
            for (x in 0 until LEVEL_WIDTH) {
                val tileIndex = levelData[y * LEVEL_WIDTH + x]
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GameLevelCanvasPreview() {
    SokobanTheme {
        GameScreen()
    }
}

