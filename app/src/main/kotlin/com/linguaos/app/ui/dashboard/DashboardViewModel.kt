package com.linguaos.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linguaos.app.data.datastore.SessionDataStore
import com.linguaos.app.data.db.dao.LanguageDao
import com.linguaos.app.data.db.dao.UnitDao
import com.linguaos.app.data.db.entity.CourseEntity
import com.linguaos.app.data.db.entity.LanguageEntity
import com.linguaos.app.data.db.entity.UnitEntity
import com.linguaos.app.data.db.entity.UserEntity
import com.linguaos.app.data.db.entity.UserProgressEntity
import com.linguaos.app.data.repository.CourseRepository
import com.linguaos.app.data.repository.UserRepository
import com.linguaos.app.util.RankUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val user: UserEntity? = null,
    val activeCourse: CourseEntity? = null,
    val targetLanguage: LanguageEntity? = null,
    val units: List<UnitEntity> = emptyList(),
    val progressMap: Map<Int, UserProgressEntity> = emptyMap(), // unitNumber -> progress
    val xpProgressFraction: Float = 0f,
    val xpToNextRank: Int? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val sessionDataStore: SessionDataStore,
    private val userRepository: UserRepository,
    private val courseRepository: CourseRepository,
    private val languageDao: LanguageDao,
    private val unitDao: UnitDao
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            sessionDataStore.loggedInUserIdFlow.filterNotNull().collect { userId ->
                userRepository.updateStreakForToday(userId)
                loadDashboard(userId)
            }
        }
    }

    private suspend fun loadDashboard(userId: Long) {
        val user = userRepository.getUser(userId) ?: return
        val courseId = user.activeCourseId

        if (courseId != null) {
            courseRepository.getCourseFlow(courseId)
                .filterNotNull()
                .collect { course ->
                    val lang = languageDao.getById(course.targetLanguageId)
                    val units = unitDao.getUnitsForLanguage(course.targetLanguageId)

                    courseRepository.getProgressForCourse(courseId)
                        .collect { progressList ->
                            val progressMap = progressList.associateBy { it.unitNumber }
                            _state.update {
                                it.copy(
                                    user = user,
                                    activeCourse = course,
                                    targetLanguage = lang,
                                    units = units,
                                    progressMap = progressMap,
                                    xpProgressFraction = RankUtils.progressToNextRank(user.totalXp),
                                    xpToNextRank = RankUtils.xpToNextRank(user.totalXp),
                                    isLoading = false
                                )
                            }
                        }
                }
        } else {
            _state.update {
                it.copy(
                    user = user,
                    isLoading = false,
                    xpProgressFraction = RankUtils.progressToNextRank(user.totalXp),
                    xpToNextRank = RankUtils.xpToNextRank(user.totalXp)
                )
            }
        }
    }
}
