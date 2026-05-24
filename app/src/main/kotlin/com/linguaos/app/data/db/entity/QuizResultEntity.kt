package com.linguaos.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_results")
data class QuizResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val courseId: String,
    val unitNumber: Int,
    val score: Int,         // 0–100
    val xpEarned: Int,
    val isPerfect: Boolean,
    val completedAt: Long = System.currentTimeMillis()
)
