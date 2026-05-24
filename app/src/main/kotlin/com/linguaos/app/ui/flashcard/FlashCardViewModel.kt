package com.linguaos.app.ui.flashcard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linguaos.app.data.datastore.SessionDataStore
import com.linguaos.app.data.db.dao.CourseDao
import com.linguaos.app.data.db.dao.UserProgressDao
import com.linguaos.app.data.db.entity.UserProgressEntity
import com.linguaos.app.data.repository.CardWithTranslation
import com.linguaos.app.data.repository.FlashCardRepository
import com.linguaos.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FlashCardUiState(
    val cards: List<CardWithTranslation> = emptyList(),
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val sessionXp: Int = 0,
    val correct: Int = 0,
    val wrong: Int = 0,
    val isComplete: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class FlashCardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sessionDataStore: SessionDataStore,
    private val flashCardRepository: FlashCardRepository,
    private val userRepository: UserRepository,
    private val userProgressDao: UserProgressDao,
    private val courseDao: CourseDao
) : ViewModel() {

    private val courseId: String   = checkNotNull(savedStateHandle["courseId"])
    private val unitNumber: Int    = checkNotNull(savedStateHandle["unitNumber"])

    private val _state = MutableStateFlow(FlashCardUiState())
    val state: StateFlow<FlashCardUiState> = _state.asStateFlow()

    private var userId: Long = 0L
    private var baseLanguageId: String = "en"
    private var targetLanguageId: String = ""

    init {
        viewModelScope.launch {
            val uid = sessionDataStore.loggedInUserIdFlow.firstOrNull() ?: return@launch
            userId = uid
            val course = courseDao.getByCourseId(courseId) ?: return@launch
            baseLanguageId   = course.baseLanguageId
            targetLanguageId = course.targetLanguageId

            val cards = flashCardRepository.getCardsForUnit(uid, courseId, targetLanguageId, baseLanguageId, unitNumber)
            _state.update { it.copy(cards = cards, isLoading = false) }
        }
    }

    fun flip() = _state.update { it.copy(isFlipped = !it.isFlipped) }

    fun swipeRight() = recordResult(correct = true)
    fun swipeLeft()  = recordResult(correct = false)

    private fun recordResult(correct: Boolean) {
        val s = _state.value
        val card = s.cards.getOrNull(s.currentIndex) ?: return
        viewModelScope.launch {
            val xp = flashCardRepository.recordResult(userId, courseId, card.state, correct)
            val newCorrect = s.correct + if (correct) 1 else 0
            val newWrong   = s.wrong   + if (correct) 0 else 1
            val nextIndex  = s.currentIndex + 1
            val done       = nextIndex >= s.cards.size

            _state.update {
                it.copy(
                    currentIndex = nextIndex,
                    isFlipped    = false,
                    sessionXp    = it.sessionXp + xp,
                    correct      = newCorrect,
                    wrong        = newWrong,
                    isComplete   = done
                )
            }
            if (done) saveProgress(newCorrect)
        }
    }

    private suspend fun saveProgress(correctCount: Int) {
        val progressId = "${userId}_${courseId}_unit${unitNumber}"
        val existing = userProgressDao.getProgress(progressId)
        val updated = (existing ?: UserProgressEntity(
            progressId = progressId,
            userId     = userId,
            courseId   = courseId,
            unitNumber = unitNumber
        )).copy(
            flashcardsCompleted = correctCount,
            lastStudiedAt       = System.currentTimeMillis()
        )
        userProgressDao.upsert(updated)
        userRepository.updateStreakForToday(userId)
    }

    fun restart() = _state.update {
        it.copy(currentIndex = 0, isFlipped = false, sessionXp = 0, correct = 0, wrong = 0, isComplete = false)
    }
}
