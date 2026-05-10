package com.libertyassistant.data.repository

import com.libertyassistant.BuildConfig
import com.libertyassistant.data.local.database.ChatDao
import com.libertyassistant.data.local.entity.ChatMessageEntity
import com.libertyassistant.data.local.entity.ChatSessionEntity
import com.libertyassistant.data.remote.AtlasDataClient
import com.libertyassistant.domain.model.ChatMessage
import com.libertyassistant.domain.model.RemoteChatSession
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatSessionRepository @Inject constructor(
    private val chatDao: ChatDao,
    private val atlasDataClient: AtlasDataClient
) {
    fun getAllLocalSessions(): Flow<List<ChatSessionEntity>> = chatDao.getAllSessions()

    suspend fun createSession(sessionId: String, mode: String) {
        chatDao.upsertSession(
            ChatSessionEntity(
                id = sessionId,
                mode = mode,
                startedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun addMessage(sessionId: String, role: String, content: String) {
        chatDao.insertMessage(
            ChatMessageEntity(sessionId = sessionId, role = role, content = content)
        )
        val count = chatDao.getMessagesForSession(sessionId).size
        chatDao.updateMessageCount(sessionId, count)
    }

    /** Marks the session as ended locally, then attempts MongoDB sync. */
    suspend fun endAndSyncSession(sessionId: String) {
        chatDao.endSession(sessionId, System.currentTimeMillis())
        syncPendingSessions()
    }

    /** Push all locally-completed, unsynced sessions to MongoDB Atlas. */
    suspend fun syncPendingSessions() {
        if (BuildConfig.MONGO_URI.isBlank()) return

        val unsynced = chatDao.getUnsyncedSessions()
        for (session in unsynced) {
            val messages = chatDao.getMessagesForSession(session.id)
            val document = AtlasDataClient.sessionToDocument(
                sessionId = session.id,
                mode = session.mode,
                startedAt = session.startedAt,
                endedAt = session.endedAt ?: System.currentTimeMillis(),
                messages = messages.map { it.role to it.content }
            )
            val ok = atlasDataClient.insertOne(
                mongoUri = BuildConfig.MONGO_URI,
                database = BuildConfig.MONGO_DATABASE,
                collection = BuildConfig.MONGO_COLLECTION,
                document = document
            )
            if (ok) chatDao.markAsSynced(session.id)
        }
    }

    /** Fetch completed sessions from MongoDB for cross-device history. */
    suspend fun fetchRemoteSessions(): List<RemoteChatSession> {
        if (BuildConfig.MONGO_URI.isBlank()) return emptyList()

        return atlasDataClient.find(
            mongoUri = BuildConfig.MONGO_URI,
            database = BuildConfig.MONGO_DATABASE,
            collection = BuildConfig.MONGO_COLLECTION
        ).map { it.toDomain() }
    }

    private fun com.libertyassistant.data.remote.RemoteSessionDto.toDomain() = RemoteChatSession(
        sessionId = sessionId,
        mode = mode,
        startedAt = startedAt,
        endedAt = endedAt,
        messageCount = messageCount,
        messages = messages.map { (role, content) -> ChatMessage(role, content) }
    )
}
