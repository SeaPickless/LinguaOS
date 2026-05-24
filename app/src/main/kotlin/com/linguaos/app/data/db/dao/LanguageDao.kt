package com.linguaos.app.data.db.dao

import androidx.room.*
import com.linguaos.app.data.db.entity.LanguageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LanguageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(languages: List<LanguageEntity>)

    @Query("SELECT * FROM languages WHERE isBaseLanguage = 1 ORDER BY name ASC")
    fun getBaseLanguagesFlow(): Flow<List<LanguageEntity>>

    @Query("SELECT * FROM languages WHERE isBaseLanguage = 1 ORDER BY name ASC")
    suspend fun getBaseLanguages(): List<LanguageEntity>

    @Query("SELECT * FROM languages WHERE isTargetLanguage = 1 ORDER BY learnerRank ASC")
    fun getTargetLanguagesFlow(): Flow<List<LanguageEntity>>

    @Query("SELECT * FROM languages WHERE isTargetLanguage = 1 ORDER BY learnerRank ASC")
    suspend fun getTargetLanguages(): List<LanguageEntity>

    @Query("SELECT * FROM languages WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): LanguageEntity?

    @Query("SELECT COUNT(*) FROM languages")
    suspend fun count(): Int
}
