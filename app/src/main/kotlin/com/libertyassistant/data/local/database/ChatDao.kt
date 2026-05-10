package com.libertyassistant.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.libertyassistant.data.local.entity.ChatMessageEntity
import com.libertyassistant.data.local.entity.ChatSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(session: ChatSessionEntity)

    @Insert
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("SELECT * FROM chat_sessions ORDER BY startedAt DESC LIMIT 100")
    fun getAllSessions(): Flow<List<ChatSessionEntity>>

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getMessagesForSession(sessionId: String): List<ChatMessageEntity>

    @Query("SELECT * FROM chat_sessions WHERE syncedToAtlas = 0 AND endedAt IS NOT NULL")
    suspend fun getUnsyncedSessions(): List<ChatSessionEntity>

    @Query("UPDATE chat_sessions SET syncedToAtlas = 1 WHERE id = :sessionId")
    suspend fun markAsSynced(sessionId: String)

    @Query("UPDATE chat_sessions SET endedAt = :endedAt WHERE id = :sessionId")
    suspend fun endSession(sessionId: String, endedAt: Long)

    @Query("UPDATE chat_sessions SET messageCount = :count WHERE id = :sessionId")
    suspend fun updateMessageCount(sessionId: String, count: Int)
}
