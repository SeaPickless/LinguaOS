package com.linguaos.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linguaos.app.data.datastore.SessionDataStore
import com.linguaos.app.data.db.entity.LanguageEntity
import com.linguaos.app.data.db.dao.LanguageDao
import com.linguaos.app.data.repository.CourseRepository
import com.linguaos.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingState(
    val step: Int = 0,                          // 0=UILang, 1=TargetLang, 2=Account, 3=Goal
    val baseLanguages: List<LanguageEntity> = emptyList(),
    val targetLanguages: List<LanguageEntity> = emptyList(),
    val selectedUiLanguageId: String = "en",
    val selectedTargetLanguageId: String = "",
    val username: String = "",
    val password: String = "",
    val dailyGoalMinutes: Int = 10,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val languageDao: LanguageDao,
    private val userRepository: UserRepository,
    private val courseRepository: CourseRepository,
    private val sessionDataStore: SessionDataStore
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val base   = languageDao.getBaseLanguages()
            val target = languageDao.getTargetLanguages()
            _state.update { it.copy(baseLanguages = base, targetLanguages = target) }
        }
    }

    fun selectUiLanguage(id: String) = _state.update { it.copy(selectedUiLanguageId = id) }
    fun selectTargetLanguage(id: String) = _state.update { it.copy(selectedTargetLanguageId = id) }
    fun updateUsername(v: String) = _state.update { it.copy(username = v, error = null) }
    fun updatePassword(v: String) = _state.update { it.copy(password = v, error = null) }
    fun selectGoal(minutes: Int) = _state.update { it.copy(dailyGoalMinutes = minutes) }

    fun nextStep() = _state.update { it.copy(step = it.step + 1) }
    fun prevStep() = _state.update { if (it.step > 0) it.copy(step = it.step - 1) else it }

    fun finishOnboarding(onSuccess: () -> Unit) {
        val s = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = userRepository.register(s.username, s.password)
            when (result) {
                is com.linguaos.app.data.repository.AuthResult.Success -> {
                    val userId = result.userId
                    // Enroll in the chosen course
                    val courseId = courseRepository.enrollInCourse(userId, s.selectedUiLanguageId, s.selectedTargetLanguageId)
                    userRepository.setActiveCourse(userId, courseId)
                    sessionDataStore.setOnboardingDone(true)
                    _state.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                is com.linguaos.app.data.repository.AuthResult.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }
}
