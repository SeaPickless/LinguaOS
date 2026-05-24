package com.linguaos.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey val progressId: String, // "<userId>_<courseId>_unit<unitNumber>"
    val userId: Long,
    val courseId: String,
    val unitNumber: Int,
    val flashcardsCompleted: Int = 0,
    val phrasesCompleted: Int = 0,
    val quizCompleted: Boolean = false,
    val quizScore: Int = 0,
    val unitXpEarned: Int = 0,
    val isUnitComplete: Boolean = false,
    val lastStudiedAt: Long = 0
)
