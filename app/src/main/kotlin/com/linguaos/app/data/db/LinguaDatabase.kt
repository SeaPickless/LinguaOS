package com.linguaos.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.linguaos.app.data.db.dao.*
import com.linguaos.app.data.db.entity.*

@Database(
    entities = [
        UserEntity::class,
        LanguageEntity::class,
        CourseEntity::class,
        UnitEntity::class,
        UserProgressEntity::class,
        FlashCardEntity::class,
        FlashCardTranslationEntity::class,
        UserFlashCardStateEntity::class,
        StreakEntity::class,
        AchievementEntity::class,
        PhraseEntity::class,
        PhraseTranslationEntity::class,
        QuizResultEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class LinguaDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun languageDao(): LanguageDao
    abstract fun courseDao(): CourseDao
    abstract fun unitDao(): UnitDao
    abstract fun userProgressDao(): UserProgressDao
    abstract fun flashCardDao(): FlashCardDao
    abstract fun streakDao(): StreakDao
    abstract fun achievementDao(): AchievementDao
    abstract fun phraseDao(): PhraseDao
    abstract fun quizResultDao(): QuizResultDao

    companion object { const val DATABASE_NAME = "lingua_db" }
}
