package com.linguaos.app.data.db.dao

import androidx.room.*
import com.linguaos.app.data.db.entity.UserProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: UserProgressEntity)

    @Query("SELECT * FROM user_progress WHERE courseId = :courseId ORDER BY unitNumber ASC")
    fun getProgressForCourse(courseId: String): Flow<List<UserProgressEntity>>

    @Query("SELECT * FROM user_progress WHERE progressId = :progressId LIMIT 1")
    suspend fun getProgress(progressId: String): UserProgressEntity?

    @Query("SELECT COUNT(*) FROM user_progress WHERE courseId = :courseId AND isUnitComplete = 1")
    suspend fun countCompletedUnits(courseId: String): Int

    @Query("SELECT SUM(unitXpEarned) FROM user_progress WHERE courseId = :courseId")
    suspend fun totalXpForCourse(courseId: String): Int?
}
