package com.linguaos.app.ui.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linguaos.app.data.datastore.SessionDataStore
import com.linguaos.app.data.db.dao.AchievementDao
import com.linguaos.app.data.db.dao.CourseDao
import com.linguaos.app.data.db.dao.FlashCardDao
import com.linguaos.app.data.db.dao.QuizResultDao
import com.linguaos.app.data.db.dao.UserProgressDao
import com.linguaos.app.data.db.entity.AchievementEntity
import com.linguaos.app.data.db.entity.QuizResultEntity
import com.linguaos.app.data.db.entity.UserProgressEntity
import com.linguaos.app.data.repository.CourseRepository
import com.linguaos.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class QuizMode { MULTIPLE_CHOICE, TYPE_ANSWER, MATCHING, SPEED_ROUND }

data class QuizQuestion(
    val cardId: String,
    val question: String,   // target word
    val answer: String,     // translation
    val options: List<String>
)

data class QuizUiState(
    val mode: QuizMode = QuizMode.MULTIPLE_CHOICE,
    val questions: List<QuizQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val selectedOption: String = "",
    val typedAnswer: String = "",
    val isAnswered: Boolean = false,
    val isCorrect: Boolean = false,
    val correctCount: Int = 0,
    val timeLeft: Int = 60,
    val isComplete: Boolean = false,
    val xpEarned: Int = 0,
    val score: Int = 0,
    val isPerfect: Boolean = false,
    val bestScore: Int = 0,
    // Matching
    val matchingLeft: List<Pair<String,String>> = emptyList(),
    val matchingRight: List<Pair<String,String>> = emptyList(),
    val matchedPairs: Set<String> = emptySet(),
    val selectedLeft: String? = null,
    val selectedRight: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class QuizViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sessionDataStore: SessionDataStore,
    private val flashCardDao: FlashCardDao,
    private val courseDao: CourseDao,
    private val quizResultDao: QuizResultDao,
    private val userProgressDao: UserProgressDao,
    private val userRepository: UserRepository,
    private val courseRepository: CourseRepository,
    private val achievementDao: AchievementDao
) : ViewModel() {

    private val courseId: String   = checkNotNull(savedStateHandle["courseId"])
    private val unitNumber: Int    = checkNotNull(savedStateHandle["unitNumber"])
    private val modeStr: String    = savedStateHandle["quizMode"] ?: "MULTIPLE_CHOICE"

    private val _state = MutableStateFlow(QuizUiState())
    val state: StateFlow<QuizUiState> = _state.asStateFlow()

    private var userId = 0L
    private var baseLanguageId = "en"
    private var targetLanguageId = ""

    init {
        viewModelScope.launch {
            userId = sessionDataStore.loggedInUserIdFlow.firstOrNull() ?: return@launch
            val course = courseDao.getByCourseId(courseId) ?: return@launch
            baseLanguageId   = course.baseLanguageId
            targetLanguageId = course.targetLanguageId
            val best = quizResultDao.bestScore(userId, courseId, unitNumber) ?: 0
            val mode = QuizMode.valueOf(modeStr)
            buildQuestions(mode, best)
        }
    }

    private suspend fun buildQuestions(mode: QuizMode, best: Int) {
        val cards = flashCardDao.getCardsForUnit(targetLanguageId, unitNumber)
        val allTranslations = cards.mapNotNull { c ->
            flashCardDao.getTranslation(c.cardId, baseLanguageId)?.translation
        }
        val questions = cards.take(if (mode == QuizMode.SPEED_ROUND) 20 else 10).mapNotNull { card ->
            val answer = flashCardDao.getTranslation(card.cardId, baseLanguageId)?.translation ?: return@mapNotNull null
            val distractors = allTranslations.filter { it != answer }.shuffled().take(3)
            QuizQuestion(
                cardId   = card.cardId,
                question = card.targetWord,
                answer   = answer,
                options  = (distractors + answer).shuffled()
            )
        }

        val matchLeft  = questions.take(4).map { it.question to it.cardId }
        val matchRight = questions.take(4).map { it.answer   to it.cardId }.shuffled()

        _state.update {
            it.copy(
                mode         = mode,
                questions    = questions,
                matchingLeft = matchLeft,
                matchingRight= matchRight,
                bestScore    = best,
                isLoading    = false
            )
        }
    }

    // ── Multiple choice ───────────────────────────────────────────────────────
    fun selectOption(option: String) {
        val q = currentQuestion() ?: return
        val correct = option.trim().equals(q.answer.trim(), ignoreCase = true)
        _state.update { it.copy(selectedOption = option, isAnswered = true, isCorrect = correct, correctCount = it.correctCount + if (correct) 1 else 0) }
    }

    // ── Type answer ───────────────────────────────────────────────────────────
    fun updateTyped(v: String) = _state.update { it.copy(typedAnswer = v) }

    fun submitTyped() {
        val q = currentQuestion() ?: return
        val input  = _state.value.typedAnswer.trim().lowercase().replace(Regex("[^a-z0-9 ]"), "")
        val target = q.answer.trim().lowercase().replace(Regex("[^a-z0-9 ]"), "")
        val correct = input == target || levenshtein(input, target) <= 1
        _state.update { it.copy(isAnswered = true, isCorrect = correct, correctCount = it.correctCount + if (correct) 1 else 0) }
    }

    // ── Matching ──────────────────────────────────────────────────────────────
    fun selectMatchLeft(word: String) = _state.update { it.copy(selectedLeft = word) }

    fun selectMatchRight(word: String) {
        val s = _state.value
        val left = s.selectedLeft ?: return
        val leftPair  = s.matchingLeft.find  { it.first == left }
        val rightPair = s.matchingRight.find { it.first == word }
        if (leftPair != null && rightPair != null && leftPair.second == rightPair.second) {
            val newMatched = s.matchedPairs + leftPair.second
            val done = newMatched.size == s.matchingLeft.size
            _state.update { it.copy(matchedPairs = newMatched, selectedLeft = null, selectedRight = null, isComplete = done, correctCount = newMatched.size) }
            if (done) viewModelScope.launch { finalise() }
        } else {
            _state.update { it.copy(selectedLeft = null, selectedRight = null) }
        }
    }

    // ── Navigation ────────────────────────────────────────────────────────────
    fun next() {
        val s = _state.value
        val nextIdx = s.currentIndex + 1
        val done = nextIdx >= s.questions.size
        _state.update { it.copy(currentIndex = nextIdx, selectedOption = "", typedAnswer = "", isAnswered = false, isCorrect = false, isComplete = done) }
        if (done) viewModelScope.launch { finalise() }
    }

    // ── Speed round tick ──────────────────────────────────────────────────────
    fun tickTimer() {
        val t = _state.value.timeLeft - 1
        if (t <= 0) {
            _state.update { it.copy(timeLeft = 0, isComplete = true) }
            viewModelScope.launch { finalise() }
        } else {
            _state.update { it.copy(timeLeft = t) }
        }
    }

    private suspend fun finalise() {
        val s = _state.value
        val total   = if (s.mode == QuizMode.MATCHING) s.matchingLeft.size else s.questions.size
        val score   = if (total > 0) s.correctCount * 100 / total else 0
        val perfect = score == 100
        val xp      = if (perfect) 20 else 10

        userRepository.addXp(userId, xp)
        courseRepository.addXpToCourse(courseId, xp)

        quizResultDao.insert(QuizResultEntity(
            userId     = userId, courseId = courseId, unitNumber = unitNumber,
            score      = score, xpEarned = xp, isPerfect = perfect
        ))

        // Update unit progress
        val progressId = "${userId}_${courseId}_unit${unitNumber}"
        val existing = userProgressDao.getProgress(progressId)
        val updated = (existing ?: UserProgressEntity(
            progressId = progressId, userId = userId,
            courseId = courseId, unitNumber = unitNumber
        )).copy(quizCompleted = true, quizScore = score, unitXpEarned = (existing?.unitXpEarned ?: 0) + xp,
                isUnitComplete = score >= 60, lastStudiedAt = System.currentTimeMillis())
        userProgressDao.upsert(updated)

        // Achievements
        val quizCount = quizResultDao.countForUser(userId)
        if (quizCount == 1) achievementDao.insert(AchievementEntity("FIRST_QUIZ", userId))
        if (perfect)        achievementDao.insert(AchievementEntity("PERFECT_QUIZ", userId))
        userRepository.updateStreakForToday(userId)

        _state.update { it.copy(score = score, xpEarned = xp, isPerfect = perfect, isComplete = true) }
    }

    private fun currentQuestion() = _state.value.questions.getOrNull(_state.value.currentIndex)

    private fun levenshtein(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) for (j in 1..b.length) {
            dp[i][j] = if (a[i-1] == b[j-1]) dp[i-1][j-1]
                       else 1 + minOf(dp[i-1][j], dp[i][j-1], dp[i-1][j-1])
        }
        return dp[a.length][b.length]
    }
}
