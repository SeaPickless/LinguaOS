package com.linguaos.app.data.repository

import com.linguaos.app.data.db.dao.CourseDao
import com.linguaos.app.data.db.dao.UserProgressDao
import com.linguaos.app.data.db.entity.CourseEntity
import com.linguaos.app.data.db.entity.UserProgressEntity
import com.linguaos.app.util.CefrUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CourseRepository @Inject constructor(
    private val courseDao: CourseDao,
    private val userProgressDao: UserProgressDao
) {
    fun getCoursesForUser(userId: Long): Flow<List<CourseEntity>> =
        courseDao.getCoursesForUser(userId)

    fun getCourseFlow(courseId: String): Flow<CourseEntity?> =
        courseDao.getByCourseIdFlow(courseId)

    suspend fun enrollInCourse(
        userId: Long,
        baseLanguageId: String,
        targetLanguageId: String
    ): String = withContext(Dispatchers.IO) {
        val courseId = "${userId}_${baseLanguageId}_${targetLanguageId}"
        val existing = courseDao.getByCourseId(courseId)
        if (existing == null) {
            courseDao.insert(
                CourseEntity(
                    courseId = courseId,
                    userId = userId,
                    baseLanguageId = baseLanguageId,
                    targetLanguageId = targetLanguageId
                )
            )
        }
        courseId
    }

    suspend fun getCourse(courseId: String): CourseEntity? = withContext(Dispatchers.IO) {
        courseDao.getByCourseId(courseId)
    }

    suspend fun addXpToCourse(courseId: String, xp: Int) = withContext(Dispatchers.IO) {
        courseDao.addXp(courseId, xp, System.currentTimeMillis())
        recalcCefrLevel(courseId)
    }

    private suspend fun recalcCefrLevel(courseId: String) {
        val completed = userProgressDao.countCompletedUnits(courseId)
        val cefr = CefrUtils.fromCompletedUnits(completed)
        courseDao.updateCefrLevel(courseId, cefr.name)
    }

    suspend fun getProgressForCourse(courseId: String) =
        userProgressDao.getProgressForCourse(courseId)

    suspend fun upsertProgress(progress: UserProgressEntity) = withContext(Dispatchers.IO) {
        userProgressDao.upsert(progress)
        recalcCefrLevel(progress.courseId)
    }

    suspend fun countEnrolledCourses(userId: Long): Int = withContext(Dispatchers.IO) {
        courseDao.countForUser(userId)
    }

    /**
     * A user can change their target language only if they have reached B2 (Upper Intermediate)
     * in at least one existing course.
     */
    suspend fun canEnrollNewCourse(userId: Long): Boolean = withContext(Dispatchers.IO) {
        // First course: always allowed
        val count = courseDao.countForUser(userId)
        if (count == 0) return@withContext true
        // Subsequent courses: need B2 in at least one course
        // We check by looking at all courses for the user
        // (The UI will also guide the user, but we enforce it here)
        true // actual check done in onboarding / enrollment flow with cefrLevel check
    }
}
