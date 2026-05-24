package com.linguaos.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "phrases")
data class PhraseEntity(
    @PrimaryKey val phraseId: String,   // "<targetLangId>_unit<n>_phrase<i>"
    val targetLanguageId: String,
    val unitNumber: Int,
    val cefrLevel: String,
    val targetPhrase: String,
    val blankedPhrase: String,          // phrase with one word replaced by ___
    val answer: String,                 // the missing word
    val option2: String,
    val option3: String,
    val option4: String,
    val wordOrder: String               // JSON array of words for word-order exercise
)
