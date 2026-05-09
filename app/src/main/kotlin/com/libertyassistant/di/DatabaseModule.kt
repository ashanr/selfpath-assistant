package com.libertyassistant.di

import android.content.Context
import androidx.room.Room
import com.libertyassistant.data.local.database.AppDatabase
import com.libertyassistant.data.local.database.JournalDao
import com.libertyassistant.data.repository.JournalRepositoryImpl
import com.libertyassistant.domain.repository.IJournalRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideJournalDao(database: AppDatabase): JournalDao = database.journalDao()

    @Provides
    @Singleton
    fun provideJournalRepository(dao: JournalDao): IJournalRepository =
        JournalRepositoryImpl(dao)
}
