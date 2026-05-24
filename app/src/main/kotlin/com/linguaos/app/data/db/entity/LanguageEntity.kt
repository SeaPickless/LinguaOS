package com.linguaos.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "languages")
data class LanguageEntity(
    @PrimaryKey val id: String,
    val name: String,
    val nativeName: String,
    val isRtl: Boolean = false,
    val isBaseLanguage: Boolean,
    val isTargetLanguage: Boolean,
    val learnerRank: Int = 0
)
