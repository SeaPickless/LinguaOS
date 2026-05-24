package com.linguaos.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flashcard_translations")
data class FlashCardTranslationEntity(
    @PrimaryKey val id: String,         // "<cardId>_<baseLanguageId>"
    val cardId: String,
    val baseLanguageId: String,
    val translation: String
)
