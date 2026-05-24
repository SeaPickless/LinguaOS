package com.linguaos.app.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val passwordHash: String,
    val deviceFingerprint: String,
    val totalXp: Int = 0,
    val currentRank: String = "NEWCOMER",
    val activeCourseId: String? = null,
    val streakCount: Int = 0,
    val lastActiveDate: String = "",
    val dailyGoalMinutes: Int = 10,
    val notificationHour: Int = 20,
    val notificationMinute: Int = 0,
    val uiLanguageId: String = "en",
    val createdAt: Long = System.currentTimeMillis()
)
