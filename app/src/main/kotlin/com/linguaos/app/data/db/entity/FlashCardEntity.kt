package com.linguaos.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flashcards")
data class FlashCardEntity(
    @PrimaryKey val cardId: String,     // "<targetLanguageId>_unit<n>_card<i>"
    val targetLanguageId: String,
    val unitNumber: Int,
    val targetWord: String,
    val transliteration: String = "",
    val audioFile: String = "",
    val cefrLevel: String
)
