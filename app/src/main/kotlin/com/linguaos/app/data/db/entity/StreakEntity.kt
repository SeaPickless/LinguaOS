package com.linguaos.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "streaks")
data class StreakEntity(
    @PrimaryKey val userId: Long,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastStreakDate: String = ""
)
