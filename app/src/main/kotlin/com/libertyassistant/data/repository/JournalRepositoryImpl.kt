package com.libertyassistant.data.repository

import com.libertyassistant.data.local.database.JournalDao
import com.libertyassistant.data.local.entity.JournalEntry as JournalEntryEntity
import com.libertyassistant.domain.model.JournalEntry
import com.libertyassistant.domain.repository.IJournalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class JournalRepositoryImpl @Inject constructor(
    private val journalDao: JournalDao
) : IJournalRepository {

    override fun getAllEntries(): Flow<List<JournalEntry>> =
        journalDao.getAllEntries().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getEntryById(id: Long): JournalEntry? =
        journalDao.getEntryById(id)?.toDomain()

    override suspend fun saveEntry(entry: JournalEntry): Long =
        journalDao.insertEntry(entry.toEntity())

    override suspend fun updateEntry(entry: JournalEntry) =
        journalDao.updateEntry(entry.toEntity())

    override suspend fun deleteEntry(id: Long) =
        journalDao.deleteEntryById(id)

    private fun JournalEntryEntity.toDomain() = JournalEntry(
        id = id,
        title = title,
        content = content,
        mode = mode,
        aiResponse = aiResponse,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun JournalEntry.toEntity() = JournalEntryEntity(
        id = id,
        title = title,
        content = content,
        mode = mode,
        aiResponse = aiResponse,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
