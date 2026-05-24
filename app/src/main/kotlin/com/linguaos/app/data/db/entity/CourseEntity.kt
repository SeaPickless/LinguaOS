package com.linguaos.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val courseId: String,   // "<userId>_<baseId>_<targetId>"
    val userId: Long,
    val baseLanguageId: String,
    val targetLanguageId: String,
    val cefrLevel: String = "NONE",
    val totalXpInCourse: Int = 0,
    val enrolledAt: Long = System.currentTimeMillis(),
    val lastStudiedAt: Long = 0
)
