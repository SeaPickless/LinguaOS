package com.linguaos.app.data.db.dao

import androidx.room.*
import com.linguaos.app.data.db.entity.AchievementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(achievement: AchievementEntity)

    @Query("SELECT * FROM achievements WHERE userId = :userId ORDER BY earnedAt DESC")
    fun getAchievementsFlow(userId: Long): Flow<List<AchievementEntity>>

    @Query("SELECT COUNT(*) FROM achievements WHERE userId = :userId AND id = :achievementId")
    suspend fun hasAchievement(userId: Long, achievementId: String): Int
}
