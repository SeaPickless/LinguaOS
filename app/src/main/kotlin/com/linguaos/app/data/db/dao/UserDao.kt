package com.linguaos.app.data.db.dao

import androidx.room.*
import com.linguaos.app.data.db.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    @Update
    suspend fun update(user: UserEntity)

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun findByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): UserEntity?

    @Query("SELECT * FROM users ORDER BY totalXp DESC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users ORDER BY totalXp DESC")
    suspend fun getAllUsers(): List<UserEntity>

    @Query("UPDATE users SET totalXp = totalXp + :xp, currentRank = :rank WHERE id = :userId")
    suspend fun addXp(userId: Long, xp: Int, rank: String)

    @Query("UPDATE users SET activeCourseId = :courseId WHERE id = :userId")
    suspend fun setActiveCourse(userId: Long, courseId: String?)

    @Query("UPDATE users SET streakCount = :streak, lastActiveDate = :date WHERE id = :userId")
    suspend fun updateStreak(userId: Long, streak: Int, date: String)

    @Query("UPDATE users SET uiLanguageId = :langId WHERE id = :userId")
    suspend fun setUiLanguage(userId: Long, langId: String)

    @Query("UPDATE users SET passwordHash = :hash WHERE id = :userId")
    suspend fun updatePasswordHash(userId: Long, hash: String)

    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    suspend fun usernameExists(username: String): Int
}
