package com.linguaos.app.di

import android.content.Context
import androidx.room.Room
import com.linguaos.app.data.db.LinguaDatabase
import com.linguaos.app.data.db.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LinguaDatabase =
        Room.databaseBuilder(context, LinguaDatabase::class.java, LinguaDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideUserDao(db: LinguaDatabase): UserDao               = db.userDao()
    @Provides fun provideLanguageDao(db: LinguaDatabase): LanguageDao       = db.languageDao()
    @Provides fun provideCourseDao(db: LinguaDatabase): CourseDao           = db.courseDao()
    @Provides fun provideUnitDao(db: LinguaDatabase): UnitDao               = db.unitDao()
    @Provides fun provideUserProgressDao(db: LinguaDatabase): UserProgressDao = db.userProgressDao()
    @Provides fun provideFlashCardDao(db: LinguaDatabase): FlashCardDao     = db.flashCardDao()
    @Provides fun provideStreakDao(db: LinguaDatabase): StreakDao           = db.streakDao()
    @Provides fun provideAchievementDao(db: LinguaDatabase): AchievementDao = db.achievementDao()
    @Provides fun providePhraseDao(db: LinguaDatabase): PhraseDao           = db.phraseDao()
    @Provides fun provideQuizResultDao(db: LinguaDatabase): QuizResultDao   = db.quizResultDao()
}
