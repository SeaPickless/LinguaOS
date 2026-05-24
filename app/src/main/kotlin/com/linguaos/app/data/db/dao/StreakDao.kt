package com.linguaos.app.data.db.dao

import androidx.room.*
import com.linguaos.app.data.db.entity.StreakEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StreakDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(streak: StreakEntity)

    @Query("SELECT * FROM streaks WHERE userId = :userId LIMIT 1")
    fun getStreakFlow(userId: Long): Flow<StreakEntity?>

    @Query("SELECT * FROM streaks WHERE userId = :userId LIMIT 1")
    suspend fun getStreak(userId: Long): StreakEntity?
}
