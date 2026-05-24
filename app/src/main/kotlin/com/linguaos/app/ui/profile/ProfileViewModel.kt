package com.linguaos.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linguaos.app.data.datastore.SessionDataStore
import com.linguaos.app.data.db.dao.AchievementDao
import com.linguaos.app.data.db.dao.FlashCardDao
import com.linguaos.app.data.db.dao.LanguageDao
import com.linguaos.app.data.db.dao.StreakDao
import com.linguaos.app.data.db.entity.*
import com.linguaos.app.data.repository.CourseRepository
import com.linguaos.app.data.repository.UserRepository
import com.linguaos.app.util.CefrLevel
import com.linguaos.app.util.CefrUtils
import com.linguaos.app.util.RankUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CourseProgress(
    val course: CourseEntity,
    val targetLanguage: LanguageEntity?,
    val cefrLevel: CefrLevel
)

data class ProfileUiState(
    val user: UserEntity? = null,
    val courseProgressList: List<CourseProgress> = emptyList(),
    val wordsLearned: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val achievementCount: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val sessionDataStore: SessionDataStore,
    private val userRepository: UserRepository,
    private val courseRepository: CourseRepository,
    private val languageDao: LanguageDao,
    private val flashCardDao: FlashCardDao,
    private val streakDao: StreakDao,
    private val achievementDao: AchievementDao
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            sessionDataStore.loggedInUserIdFlow.filterNotNull().collect { userId ->
                loadProfile(userId)
            }
        }
    }

    private suspend fun loadProfile(userId: Long) {
        val user = userRepository.getUser(userId) ?: return
        val streak = streakDao.getStreak(userId)
        val wordsLearned = flashCardDao.countLearnedCards(userId)
        val achievementCount = achievementDao.getAchievementsFlow(userId).firstOrNull()?.size ?: 0

        courseRepository.getCoursesForUser(userId).collect { courses ->
            val courseProgress = courses.map { course ->
                val lang = languageDao.getById(course.targetLanguageId)
                CourseProgress(
                    course = course,
                    targetLanguage = lang,
                    cefrLevel = CefrUtils.fromCompletedUnits(
                        course.cefrLevel.let {
                            when (it) {
                                "C1" -> 10; "B2" -> 8; "B1" -> 5
                                "A2" -> 3; "A1" -> 1; else -> 0
                            }
                        }
                    )
                )
            }
            _state.update {
                it.copy(
                    user = user,
                    courseProgressList = courseProgress,
                    wordsLearned = wordsLearned,
                    currentStreak = streak?.currentStreak ?: 0,
                    longestStreak = streak?.longestStreak ?: 0,
                    achievementCount = achievementCount,
                    isLoading = false
                )
            }
        }
    }
}
