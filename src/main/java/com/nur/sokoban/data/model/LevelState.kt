package com.nur.sokoban.data.model

data class LevelState(
    val levelNumber: Int,
    val grid: IntArray,
    val playerX: Int,
    val playerY: Int,
    val heroOnGoal: Boolean,
    val moveCount: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LevelState

        if (levelNumber != other.levelNumber) return false
        if (playerX != other.playerX) return false
        if (playerY != other.playerY) return false
        if (heroOnGoal != other.heroOnGoal) return false
        if (moveCount != other.moveCount) return false
        if (!grid.contentEquals(other.grid)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = levelNumber
        result = 31 * result + playerX
        result = 31 * result + playerY
        result = 31 * result + heroOnGoal.hashCode()
        result = 31 * result + moveCount
        result = 31 * result + grid.contentHashCode()
        return result
    }
}
