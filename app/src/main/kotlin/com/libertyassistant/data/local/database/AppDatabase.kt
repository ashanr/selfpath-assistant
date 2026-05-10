package com.libertyassistant.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.libertyassistant.data.local.entity.ChatMessageEntity
import com.libertyassistant.data.local.entity.ChatSessionEntity
import com.libertyassistant.data.local.entity.JournalEntry

@Database(
    entities = [JournalEntry::class, ChatSessionEntity::class, ChatMessageEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun journalDao(): JournalDao
    abstract fun chatDao(): ChatDao

    companion object {
        const val DATABASE_NAME = "liberty_assistant_db"
    }
}
