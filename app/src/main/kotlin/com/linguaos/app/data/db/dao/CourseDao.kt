package com.linguaos.app.data.db.dao

import androidx.room.*
import com.linguaos.app.data.db.entity.CourseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(course: CourseEntity)

    @Update
    suspend fun update(course: CourseEntity)

    @Query("SELECT * FROM courses WHERE userId = :userId ORDER BY lastStudiedAt DESC")
    fun getCoursesForUser(userId: Long): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE courseId = :courseId LIMIT 1")
    suspend fun getByCourseId(courseId: String): CourseEntity?

    @Query("SELECT * FROM courses WHERE courseId = :courseId LIMIT 1")
    fun getByCourseIdFlow(courseId: String): Flow<CourseEntity?>

    @Query("UPDATE courses SET cefrLevel = :level WHERE courseId = :courseId")
    suspend fun updateCefrLevel(courseId: String, level: String)

    @Query("UPDATE courses SET totalXpInCourse = totalXpInCourse + :xp, lastStudiedAt = :time WHERE courseId = :courseId")
    suspend fun addXp(courseId: String, xp: Int, time: Long)

    @Query("SELECT COUNT(*) FROM courses WHERE userId = :userId")
    suspend fun countForUser(userId: Long): Int
}
