package com.linguaos.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_flashcard_states")
data class UserFlashCardStateEntity(
    @PrimaryKey val stateId: String,    // "<userId>_<cardId>"
    val userId: Long,
    val cardId: String,
    val repetitions: Int = 0,
    val easeFactor: Float = 2.5f,
    val intervalDays: Int = 0,
    val nextReviewDate: String = "",
    val isLearned: Boolean = false,
    val lastResult: Int = 0
)
