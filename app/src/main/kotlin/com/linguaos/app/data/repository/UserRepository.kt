package com.linguaos.app.data.repository

import android.content.Context
import com.linguaos.app.data.datastore.SessionDataStore
import com.linguaos.app.data.db.dao.AchievementDao
import com.linguaos.app.data.db.dao.CourseDao
import com.linguaos.app.data.db.dao.StreakDao
import com.linguaos.app.data.db.dao.UserDao
import com.linguaos.app.data.db.entity.AchievementEntity
import com.linguaos.app.data.db.entity.StreakEntity
import com.linguaos.app.data.db.entity.UserEntity
import com.linguaos.app.util.DateUtils
import com.linguaos.app.util.DeviceUtils
import com.linguaos.app.util.RankUtils
import com.linguaos.app.util.SecurityUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult {
    data class Success(val userId: Long) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

@Singleton
class UserRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userDao: UserDao,
    private val streakDao: StreakDao,
    private val achievementDao: AchievementDao,
    private val sessionDataStore: SessionDataStore
) {
    val currentUserIdFlow = sessionDataStore.loggedInUserIdFlow

    suspend fun register(username: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        if (username.isBlank()) return@withContext AuthResult.Error("Username cannot be empty")
        if (!username.matches(Regex("[A-Za-z0-9_]+"))) return@withContext AuthResult.Error("Username can only contain letters, numbers, and underscores")
        if (password.length < 6) return@withContext AuthResult.Error("Password must be at least 6 characters")
        if (userDao.usernameExists(username) > 0) return@withContext AuthResult.Error("Username is already taken")
        val hash = SecurityUtils.hashPassword(password, username)
        val fingerprint = DeviceUtils.getDeviceFingerprint(context)
        val id = userDao.insert(UserEntity(username = username, passwordHash = hash, deviceFingerprint = fingerprint))
        streakDao.upsert(StreakEntity(userId = id))
        sessionDataStore.setLoggedInUser(id, username)
        AuthResult.Success(id)
    }

    suspend fun login(username: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        val user = userDao.findByUsername(username) ?: return@withContext AuthResult.Error("Invalid username or password")
        if (!SecurityUtils.verifyPassword(password, username, user.passwordHash)) return@withContext AuthResult.Error("Invalid username or password")
        sessionDataStore.setLoggedInUser(user.id, user.username)
        AuthResult.Success(user.id)
    }

    suspend fun loginAs(userId: Long): Boolean = withContext(Dispatchers.IO) {
        val user = userDao.findById(userId) ?: return@withContext false
        sessionDataStore.setLoggedInUser(user.id, user.username)
        true
    }

    suspend fun logout() { sessionDataStore.clearSession() }

    suspend fun getUser(userId: Long): UserEntity? = withContext(Dispatchers.IO) { userDao.findById(userId) }

    suspend fun getAllUsers(): List<UserEntity> = withContext(Dispatchers.IO) { userDao.getAllUsers() }

    fun getAllUsersFlow() = userDao.getAllUsersFlow()

    suspend fun addXp(userId: Long, xp: Int): Unit = withContext(Dispatchers.IO) {
        val user = userDao.findById(userId) ?: return@withContext
        val newXp  = user.totalXp + xp
        val newRank = RankUtils.fromXp(newXp)
        userDao.addXp(userId, xp, newRank.name)
        // Rank achievements
        if (newRank == com.linguaos.app.util.Rank.ADVANCED) achievementDao.insert(AchievementEntity("RANK_ADVANCED", userId))
        if (newRank == com.linguaos.app.util.Rank.LEGEND)   achievementDao.insert(AchievementEntity("RANK_LEGEND",   userId))
    }

    suspend fun setActiveCourse(userId: Long, courseId: String?) = withContext(Dispatchers.IO) {
        userDao.setActiveCourse(userId, courseId)
    }

    suspend fun updateStreakForToday(userId: Long) = withContext(Dispatchers.IO) {
        val user = userDao.findById(userId) ?: return@withContext
        val today = DateUtils.today()
        if (DateUtils.isToday(user.lastActiveDate)) return@withContext
        val newStreak = if (DateUtils.isYesterday(user.lastActiveDate)) user.streakCount + 1 else 1
        userDao.updateStreak(userId, newStreak, today)
        val streak = streakDao.getStreak(userId)
        streakDao.upsert(StreakEntity(userId = userId, currentStreak = newStreak, longestStreak = maxOf(newStreak, streak?.longestStreak ?: 0), lastStreakDate = today))
        if (newStreak >= 7)  achievementDao.insert(AchievementEntity("STREAK_7",  userId))
        if (newStreak >= 30) achievementDao.insert(AchievementEntity("STREAK_30", userId))
    }

    suspend fun resetPassword(username: String, newPassword: String): AuthResult = withContext(Dispatchers.IO) {
        val user = userDao.findByUsername(username) ?: return@withContext AuthResult.Error("Invalid username")
        val currentFp = DeviceUtils.getDeviceFingerprint(context)
        if (user.deviceFingerprint != currentFp) return@withContext AuthResult.Error("Cannot recover on a different device")
        if (newPassword.length < 6) return@withContext AuthResult.Error("Password must be at least 6 characters")
        userDao.updatePasswordHash(user.id, SecurityUtils.hashPassword(newPassword, username))
        AuthResult.Success(user.id)
    }
}
