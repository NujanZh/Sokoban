package com.nur.sokoban.data.model

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object ProgressManager {
    private const val PREFS_NAME = "sokoban_progress"
    private const val KEY_LEVEL_SCORE = "level_%d_score"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveLevelScore(context: Context, levelNumber: Int, score: Int) {
        val prefs = getPrefs(context)
        val currentBest = getLevelScore(context, levelNumber)
    }

    fun getLevelScore(context: Context, levelNumber: Int): Int {
        val prefs = getPrefs(context)
        val score = prefs.getInt(KEY_LEVEL_SCORE.format(levelNumber), -1)
        return if (score == -1) 0 else score
    }

    fun loadScoresForLevels(context: Context, levels: List<Level>) {
        levels.forEach { level ->
            val bestScore = getLevelScore(context, level.number)
        }
    }

    fun clearAllProgress(context: Context) {
        getPrefs(context).edit { clear() }
    }
}

