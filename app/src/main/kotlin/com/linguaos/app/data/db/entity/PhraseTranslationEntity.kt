package com.linguaos.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "phrase_translations")
data class PhraseTranslationEntity(
    @PrimaryKey val id: String,         // "<phraseId>_<baseLanguageId>"
    val phraseId: String,
    val baseLanguageId: String,
    val translation: String
)
