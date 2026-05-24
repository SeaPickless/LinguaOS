package com.linguaos.app.ui.practice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linguaos.app.data.datastore.SessionDataStore
import com.linguaos.app.data.db.dao.CourseDao
import com.linguaos.app.data.db.dao.PhraseDao
import com.linguaos.app.data.db.dao.UserProgressDao
import com.linguaos.app.data.db.entity.PhraseEntity
import com.linguaos.app.data.db.entity.UserProgressEntity
import com.linguaos.app.data.repository.CourseRepository
import com.linguaos.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PracticeMode { FILL_BLANK, WORD_ORDER }

data class PracticeItem(
    val phrase: PhraseEntity,
    val translation: String,
    val mode: PracticeMode,
    val options: List<String>,          // for FILL_BLANK
    val wordTokens: List<String>        // for WORD_ORDER
)

data class PracticeUiState(
    val items: List<PracticeItem> = emptyList(),
    val currentIndex: Int = 0,
    val selectedAnswer: String = "",
    val placedWords: List<String> = emptyList(),
    val availableWords: List<String> = emptyList(),
    val isAnswered: Boolean = false,
    val isCorrect: Boolean = false,
    val sessionXp: Int = 0,
    val correct: Int = 0,
    val isComplete: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class PracticeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sessionDataStore: SessionDataStore,
    private val phraseDao: PhraseDao,
    private val courseDao: CourseDao,
    private val userProgressDao: UserProgressDao,
    private val userRepository: UserRepository,
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val courseId: String = checkNotNull(savedStateHandle["courseId"])
    private val unitNumber: Int  = checkNotNull(savedStateHandle["unitNumber"])

    private val _state = MutableStateFlow(PracticeUiState())
    val state: StateFlow<PracticeUiState> = _state.asStateFlow()

    private var userId = 0L

    init {
        viewModelScope.launch {
            userId = sessionDataStore.loggedInUserIdFlow.firstOrNull() ?: return@launch
            val course = courseDao.getByCourseId(courseId) ?: return@launch
            val phrases = phraseDao.getPhrasesForUnit(course.targetLanguageId, unitNumber)
            val items = phrases.mapIndexed { i, p ->
                val translation = phraseDao.getTranslation(p.phraseId, course.baseLanguageId)?.translation ?: ""
                val mode = if (i % 2 == 0) PracticeMode.FILL_BLANK else PracticeMode.WORD_ORDER
                val wordTokens = p.wordOrder.removeSurrounding("[", "]")
                    .split(",").map { it.trim().removeSurrounding("\"") }.filter { it.isNotBlank() }
                PracticeItem(
                    phrase      = p,
                    translation = translation,
                    mode        = mode,
                    options     = listOf(p.answer, p.option2, p.option3, p.option4).shuffled(),
                    wordTokens  = wordTokens.shuffled()
                )
            }
            val first = items.firstOrNull()
            _state.update {
                it.copy(
                    items          = items,
                    availableWords = first?.wordTokens ?: emptyList(),
                    isLoading      = false
                )
            }
        }
    }

    fun selectOption(option: String) {
        val item = currentItem() ?: return
        val correct = option == item.phrase.answer
        _state.update { it.copy(selectedAnswer = option, isAnswered = true, isCorrect = correct) }
    }

    fun tapWord(word: String) {
        val s = _state.value
        val newAvail = s.availableWords.toMutableList().also { it.remove(word) }
        val newPlaced = s.placedWords + word
        val item = currentItem() ?: return
        val correct = newPlaced.joinToString(" ") == item.phrase.answer
        _state.update {
            it.copy(
                availableWords = newAvail,
                placedWords    = newPlaced,
                isAnswered     = newAvail.isEmpty(),
                isCorrect      = correct
            )
        }
    }

    fun removePlacedWord(word: String) {
        val s = _state.value
        if (s.isAnswered) return
        val newPlaced = s.placedWords.toMutableList().also { it.remove(word) }
        _state.update { it.copy(placedWords = newPlaced, availableWords = it.availableWords + word) }
    }

    fun next() {
        val s = _state.value
        val xpGain = if (s.isCorrect) 5 else 0
        val nextIdx = s.currentIndex + 1
        val done = nextIdx >= s.items.size
        viewModelScope.launch {
            if (s.isCorrect) {
                userRepository.addXp(userId, xpGain)
                courseRepository.addXpToCourse(courseId, xpGain)
            }
            val nextItem = s.items.getOrNull(nextIdx)
            _state.update {
                it.copy(
                    currentIndex   = nextIdx,
                    selectedAnswer = "",
                    placedWords    = emptyList(),
                    availableWords = nextItem?.wordTokens ?: emptyList(),
                    isAnswered     = false,
                    isCorrect      = false,
                    sessionXp      = it.sessionXp + xpGain,
                    correct        = it.correct + if (s.isCorrect) 1 else 0,
                    isComplete     = done
                )
            }
            if (done) saveProgress()
        }
    }

    private suspend fun saveProgress() {
        val s = _state.value
        val progressId = "${userId}_${courseId}_unit${unitNumber}"
        val existing = userProgressDao.getProgress(progressId)
        val updated = (existing ?: UserProgressEntity(
            progressId = progressId, userId = userId,
            courseId = courseId, unitNumber = unitNumber
        )).copy(phrasesCompleted = s.correct, lastStudiedAt = System.currentTimeMillis())
        userProgressDao.upsert(updated)
        userRepository.updateStreakForToday(userId)
    }

    private fun currentItem() = _state.value.items.getOrNull(_state.value.currentIndex)

    fun restart() = _state.update { it.copy(currentIndex = 0, selectedAnswer = "", placedWords = emptyList(), isAnswered = false, isCorrect = false, sessionXp = 0, correct = 0, isComplete = false) }
}
