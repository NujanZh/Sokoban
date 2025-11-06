package com.nur.sokoban.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.nur.sokoban.R
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun LevelCanvas(
    levelData: List<Int>,
    levelWidth: Int,
    levelHeight: Int,
    modifier: Modifier = Modifier,
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

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val tileWidthPx = canvasWidth / levelWidth
        val tileHeightPx = canvasHeight / levelHeight
        val tileSize = min(tileWidthPx, tileHeightPx)

        val offsetX = (canvasWidth - (levelWidth * tileSize)) / 2
        val offsetY = (canvasHeight - (levelHeight * tileSize)) / 2

        for (y in 0 until levelHeight) {
            for (x in 0 until levelWidth) {
                val displayValue = levelData[y * levelWidth + x]

                val img: ImageBitmap = tiles[displayValue.coerceIn(0, tiles.lastIndex)]

                val posX = offsetX + x * tileSize
                val posY = offsetY + y * tileSize

                drawImage(
                    image = img,
                    dstOffset = IntOffset(
                        x = posX.roundToInt(),
                        y = posY.roundToInt()
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