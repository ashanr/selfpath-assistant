package com.libertyassistant.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.libertyassistant.data.local.entity.JournalEntry

@Database(
    entities = [JournalEntry::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun journalDao(): JournalDao

    companion object {
        const val DATABASE_NAME = "liberty_assistant_db"
    }
}
