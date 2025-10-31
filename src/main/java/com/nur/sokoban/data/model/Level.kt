package com.nur.sokoban.data.model

data class Level(
    val number: Int,
    val name: String,
    val grid: IntArray,
    val width: Int,
    val height: Int,
    val playerStartX: Int,
    val playerStartY: Int,
    val playerOnGoalAtStart: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Level

        if (number != other.number) return false
        if (width != other.width) return false
        if (height != other.height) return false
        if (playerStartX != other.playerStartX) return false
        if (playerStartY != other.playerStartY) return false
        if (playerOnGoalAtStart != other.playerOnGoalAtStart) return false
        if (name != other.name) return false
        if (!grid.contentEquals(other.grid)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = number
        result = 31 * result + width
        result = 31 * result + height
        result = 31 * result + playerStartX
        result = 31 * result + playerStartY
        result = 31 * result + playerOnGoalAtStart.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + grid.contentHashCode()
        return result
    }
}

object LevelParser {
    fun parseLevels(content: String): List<Level> {
        val levels = mutableListOf<Level>()
        val lines = content.lines()

        var i = 0
        while (i < lines.size) {
            val line = lines[i].trim()

            if (line.startsWith("Level ")) {
                val levelNumber = line.substringAfter("Level ").toIntOrNull() ?: 0
                i++

                while (i < lines.size && (lines[i].trim().startsWith(";")) ||
                    lines[i].trim().startsWith("'")) {
                    i++
                }

                val mapLines = mutableListOf<String>()
                while (i < lines.size && lines[i].trim().isNotEmpty() &&
                    !lines[i].trim().startsWith("Level")) {
                    val mapLine = lines[i]
                    if (mapLine.any {it in " #.$*@+"}) {
                        mapLines.add(mapLine)
                    }
                    i++
                }

                if (mapLines.isNotEmpty()) {
                    val level = parseLevel(levelNumber, mapLines)
                    if (level != null) {
                        levels.add(level)
                    }
                }
            } else {
                i++
            }
        }
        return levels
    }

    private fun parseLevel(number: Int, mapLines: List<String>): Level? {
        if (mapLines.isEmpty()) return null

        val height = mapLines.size
        val width = mapLines.maxOfOrNull { it.length } ?: 0

        val grid = IntArray(width * height) {0}
        var playerX = 0
        var playerY = 0
        var playerOnGoal = false

        for (y in mapLines.indices) {
            val line = mapLines[y]
            for (x in line.indices) {
                val char = line[x]
                val index = y * width + x

                grid[index] = when (char) {
                    ' ' -> 0
                    '#' -> 1
                    '$' -> 2
                    '.' -> 3
                    '@' -> {
                        playerX = x
                        playerY = y
                        playerOnGoal = false
                        4
                    }
                    '*' -> 5
                    '+' -> {
                        playerX = x
                        playerY = y
                        playerOnGoal = true
                        4
                    }
                    else -> 0
                }
            }
        }

        return Level(
            number = number,
            name = "NABOKOSMOS ${number.toString().padStart(2, '0')}",
            grid = grid,
            width = width,
            height = height,
            playerStartX = playerX,
            playerStartY = playerY,
            playerOnGoalAtStart = playerOnGoal
        )
    }
}