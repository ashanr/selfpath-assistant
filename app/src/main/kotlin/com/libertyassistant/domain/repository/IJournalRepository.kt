package com.libertyassistant.domain.repository

import com.libertyassistant.domain.model.JournalEntry
import kotlinx.coroutines.flow.Flow

interface IJournalRepository {
    fun getAllEntries(): Flow<List<JournalEntry>>
    suspend fun getEntryById(id: Long): JournalEntry?
    suspend fun saveEntry(entry: JournalEntry): Long
    suspend fun updateEntry(entry: JournalEntry)
    suspend fun deleteEntry(id: Long)
}
