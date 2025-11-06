package com.nur.sokoban.data.model

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject

object ProgressManager {
    private const val PREFS_NAME = "sokoban_progress"
    private const val KEY_LEVEL_SCORE = "level_%d_score"
    private const val KEY_LEVEL_STATE = "level_%d_state"

    fun saveLevelScore(context: Context, levelNumber: Int, score: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentBest = getLevelScore(context, levelNumber)

        if (currentBest == 0 || score < currentBest) {
            prefs.edit {
                putInt(KEY_LEVEL_SCORE.format(levelNumber), score)
            }
        }

        clearLevelState(context, levelNumber)
    }

    fun getLevelScore(context: Context, levelNumber: Int): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val score = prefs.getInt(KEY_LEVEL_SCORE.format(levelNumber), -1)
        return if (score == -1) 0 else score
    }

    fun loadScoresForLevels(context: Context, levels: List<Level>) {
        levels.forEach { level ->
            val bestScore = getLevelScore(context, level.number)
            level.bestScore = if (bestScore > 0) bestScore else null
        }
    }

    fun clearAllProgress(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { clear() }
    }

    fun saveLevelState(context: Context, state: LevelState) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = JSONObject().apply {
            put("levelNumber", state.levelNumber)
            put("playerX", state.playerX)
            put("playerY", state.playerY)
            put("heroOnGoal", state.heroOnGoal)
            put("moveCount", state.moveCount)
            put("grid", JSONArray(state.grid.toList()))
        }

        prefs.edit {
            putString(KEY_LEVEL_STATE.format(state.levelNumber), json.toString())
        }
    }

    fun loadLevelState(context: Context, levelNumber: Int): LevelState? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(KEY_LEVEL_STATE.format(levelNumber), null) ?: return null

        return try {
            val json = JSONObject(jsonString)
            val gridArray = json.getJSONArray("grid")
            val grid = IntArray(gridArray.length()) { i -> gridArray.getInt(i) }

            LevelState(
                levelNumber = json.getInt("levelNumber"),
                playerX = json.getInt("playerX"),
                playerY = json.getInt("playerY"),
                heroOnGoal = json.getBoolean("heroOnGoal"),
                moveCount = json.getInt("moveCount"),
                grid = grid
            )
        } catch (e: Exception) {
            null
        }
    }

    fun clearLevelState(context: Context, levelNumber: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            remove(KEY_LEVEL_STATE.format(levelNumber))
        }
    }

}

