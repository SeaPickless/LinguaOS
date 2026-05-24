package com.linguaos.app.data.repository

import com.linguaos.app.data.db.dao.AchievementDao
import com.linguaos.app.data.db.dao.FlashCardDao
import com.linguaos.app.data.db.entity.AchievementEntity
import com.linguaos.app.data.db.entity.FlashCardEntity
import com.linguaos.app.data.db.entity.UserFlashCardStateEntity
import com.linguaos.app.util.Sm2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class CardWithTranslation(
    val card: FlashCardEntity,
    val translation: String,
    val state: UserFlashCardStateEntity
)

@Singleton
class FlashCardRepository @Inject constructor(
    private val flashCardDao: FlashCardDao,
    private val achievementDao: AchievementDao,
    private val userRepository: UserRepository,
    private val courseRepository: CourseRepository
) {
    /** Load all cards for a unit with their translation and SRS state. */
    suspend fun getCardsForUnit(
        userId: Long,
        courseId: String,
        targetLangId: String,
        baseLanguageId: String,
        unitNumber: Int
    ): List<CardWithTranslation> = withContext(Dispatchers.IO) {
        val cards = flashCardDao.getCardsForUnit(targetLangId, unitNumber)
        cards.map { card ->
            val translation = flashCardDao.getTranslation(card.cardId, baseLanguageId)?.translation ?: card.targetWord
            val stateId = "${userId}_${card.cardId}"
            val state = flashCardDao.getState(stateId) ?: UserFlashCardStateEntity(
                stateId        = stateId,
                userId         = userId,
                cardId         = card.cardId,
                nextReviewDate = Sm2.todayString()
            )
            CardWithTranslation(card, translation, state)
        }
    }

    /** Called when user swipes right (correct) or left (wrong). Returns XP earned. */
    suspend fun recordResult(
        userId: Long,
        courseId: String,
        state: UserFlashCardStateEntity,
        correct: Boolean
    ): Int = withContext(Dispatchers.IO) {
        val xp = if (correct) 2 else 1
        val newState = Sm2.advance(state, if (correct) 1 else 0)
        flashCardDao.upsertState(newState)
        userRepository.addXp(userId, xp)
        courseRepository.addXpToCourse(courseId, xp)
        // First flashcard achievement
        val learned = flashCardDao.countLearnedCards(userId)
        if (learned == 1) {
            achievementDao.insert(AchievementEntity("FIRST_FLASHCARD", userId))
        }
        if (learned >= 100) {
            achievementDao.insert(AchievementEntity("WORDS_100", userId))
        }
        xp
    }

    suspend fun getDueCards(userId: Long): List<UserFlashCardStateEntity> =
        withContext(Dispatchers.IO) {
            flashCardDao.getDueCards(userId, Sm2.todayString())
        }

    suspend fun countLearned(userId: Long): Int =
        withContext(Dispatchers.IO) { flashCardDao.countLearnedCards(userId) }
}
