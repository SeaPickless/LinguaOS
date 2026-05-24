package com.linguaos.app.data.db.dao

import androidx.room.*
import com.linguaos.app.data.db.entity.FlashCardEntity
import com.linguaos.app.data.db.entity.FlashCardTranslationEntity
import com.linguaos.app.data.db.entity.UserFlashCardStateEntity

@Dao
interface FlashCardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<FlashCardEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranslations(translations: List<FlashCardTranslationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertState(state: UserFlashCardStateEntity)

    @Query("SELECT * FROM flashcards WHERE targetLanguageId = :langId AND unitNumber = :unit ORDER BY cardId ASC")
    suspend fun getCardsForUnit(langId: String, unit: Int): List<FlashCardEntity>

    @Query("SELECT * FROM flashcard_translations WHERE cardId = :cardId AND baseLanguageId = :baseLang LIMIT 1")
    suspend fun getTranslation(cardId: String, baseLang: String): FlashCardTranslationEntity?

    @Query("SELECT * FROM user_flashcard_states WHERE stateId = :stateId LIMIT 1")
    suspend fun getState(stateId: String): UserFlashCardStateEntity?

    @Query("SELECT * FROM user_flashcard_states WHERE userId = :userId AND nextReviewDate <= :today ORDER BY nextReviewDate ASC LIMIT 20")
    suspend fun getDueCards(userId: Long, today: String): List<UserFlashCardStateEntity>

    @Query("SELECT COUNT(*) FROM user_flashcard_states WHERE userId = :userId AND isLearned = 1")
    suspend fun countLearnedCards(userId: Long): Int
}
