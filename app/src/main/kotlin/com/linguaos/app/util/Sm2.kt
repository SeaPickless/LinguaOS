package com.linguaos.app.util

import com.linguaos.app.data.db.entity.UserFlashCardStateEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object Sm2 {
    private val fmt = DateTimeFormatter.ISO_LOCAL_DATE

    /**
     * Run one SM-2 iteration.
     * quality: 0 = wrong, 1 = correct
     * Returns updated state with next review date set.
     */
    fun advance(state: UserFlashCardStateEntity, quality: Int): UserFlashCardStateEntity {
        val q = quality.coerceIn(0, 1)
        val newReps: Int
        val newInterval: Int
        val newEase: Float

        if (q == 0) {
            // Wrong: reset repetitions, review tomorrow
            newReps     = 0
            newInterval = 1
            newEase     = maxOf(1.3f, state.easeFactor - 0.2f)
        } else {
            newReps = state.repetitions + 1
            newInterval = when (state.repetitions) {
                0    -> 1
                1    -> 6
                else -> (state.intervalDays * state.easeFactor).toInt().coerceAtLeast(1)
            }
            newEase = (state.easeFactor + 0.1f).coerceAtMost(2.5f)
        }

        val nextDate = LocalDate.now().plusDays(newInterval.toLong()).format(fmt)
        return state.copy(
            repetitions    = newReps,
            intervalDays   = newInterval,
            easeFactor     = newEase,
            nextReviewDate = nextDate,
            isLearned      = newReps >= 3,
            lastResult     = q
        )
    }

    fun todayString(): String = LocalDate.now().format(fmt)
}
