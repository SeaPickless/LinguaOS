package com.linguaos.app.ui.units

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linguaos.app.data.datastore.SessionDataStore
import com.linguaos.app.data.db.dao.CourseDao
import com.linguaos.app.data.db.dao.LanguageDao
import com.linguaos.app.data.db.dao.UnitDao
import com.linguaos.app.data.db.entity.UnitEntity
import com.linguaos.app.data.db.entity.UserProgressEntity
import com.linguaos.app.data.repository.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UnitListUiState(
    val courseId: String = "",
    val targetLanguageName: String = "",
    val units: List<UnitEntity> = emptyList(),
    val progressMap: Map<Int, UserProgressEntity> = emptyMap(),
    val isLoading: Boolean = true
)

@HiltViewModel
class UnitListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sessionDataStore: SessionDataStore,
    private val courseDao: CourseDao,
    private val languageDao: LanguageDao,
    private val unitDao: UnitDao,
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val courseId: String = checkNotNull(savedStateHandle["courseId"])
    private val _state = MutableStateFlow(UnitListUiState(courseId = courseId))
    val state: StateFlow<UnitListUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val course = courseDao.getByCourseId(courseId) ?: return@launch
            val lang   = languageDao.getById(course.targetLanguageId)
            val units  = unitDao.getUnitsForLanguage(course.targetLanguageId)
            courseRepository.getProgressForCourse(courseId).collect { progressList ->
                _state.update {
                    it.copy(
                        targetLanguageName = lang?.name ?: "",
                        units       = units,
                        progressMap = progressList.associateBy { p -> p.unitNumber },
                        isLoading   = false
                    )
                }
            }
        }
    }
}
