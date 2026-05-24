package com.linguaos.app.data.db.dao

import androidx.room.*
import com.linguaos.app.data.db.entity.QuizResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizResultDao {
    @Insert
    suspend fun insert(result: QuizResultEntity)

    @Query("SELECT * FROM quiz_results WHERE userId = :userId ORDER BY completedAt DESC")
    fun getResultsForUser(userId: Long): Flow<List<QuizResultEntity>>

    @Query("SELECT COUNT(*) FROM quiz_results WHERE userId = :userId")
    suspend fun countForUser(userId: Long): Int

    @Query("SELECT MAX(score) FROM quiz_results WHERE userId = :userId AND courseId = :courseId AND unitNumber = :unit")
    suspend fun bestScore(userId: Long, courseId: String, unit: Int): Int?
}
