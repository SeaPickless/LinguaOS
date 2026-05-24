package com.linguaos.app.data.db.dao

import androidx.room.*
import com.linguaos.app.data.db.entity.PhraseEntity
import com.linguaos.app.data.db.entity.PhraseTranslationEntity

@Dao
interface PhraseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(phrases: List<PhraseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranslations(list: List<PhraseTranslationEntity>)

    @Query("SELECT * FROM phrases WHERE targetLanguageId = :langId AND unitNumber = :unit ORDER BY phraseId ASC")
    suspend fun getPhrasesForUnit(langId: String, unit: Int): List<PhraseEntity>

    @Query("SELECT * FROM phrase_translations WHERE phraseId = :phraseId AND baseLanguageId = :baseLang LIMIT 1")
    suspend fun getTranslation(phraseId: String, baseLang: String): PhraseTranslationEntity?

    @Query("SELECT COUNT(*) FROM phrases")
    suspend fun count(): Int
}
