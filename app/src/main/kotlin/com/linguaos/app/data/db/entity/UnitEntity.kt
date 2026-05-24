package com.linguaos.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "units")
data class UnitEntity(
    @PrimaryKey val unitId: String,     // "<targetLanguageId>_unit<number>"
    val targetLanguageId: String,
    val unitNumber: Int,
    val nameKey: String,
    val cefrLevel: String,
    val flashcardCount: Int = 20,
    val phraseCount: Int = 10,
    val quizCount: Int = 1
)
