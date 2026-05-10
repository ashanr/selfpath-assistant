package com.libertyassistant.data.remote

import com.mongodb.kotlin.client.coroutine.MongoClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import org.bson.Document
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MongoDB client using the official Kotlin Coroutines driver.
 * Connects directly to an Atlas cluster via a mongodb+srv:// URI —
 * no Atlas App Services / App ID required.
 *
 * The user supplies the full connection URI in Settings, e.g.:
 *   mongodb+srv://ashanpri:<password>@libertyassistantcluster.kd0ecbv.mongodb.net/?appName=LibertyAssistantCluster
 *
 * Prerequisites on the Atlas side:
 *   • Network Access → allow 0.0.0.0/0 (or the device's IP) in the IP Allowlist.
 *   • Database Access → a user with readWrite on the target database.
 */
@Singleton
class AtlasDataClient @Inject constructor() {

    suspend fun insertOne(
        mongoUri: String,
        database: String,
        collection: String,
        document: Document
    ): Boolean = withContext(Dispatchers.IO) {
        if (mongoUri.isBlank()) return@withContext false
        runCatching {
            MongoClient.create(mongoUri).use { client ->
                client.getDatabase(database)
                    .getCollection<Document>(collection)
                    .insertOne(document)
            }
            true
        }.getOrDefault(false)
    }

    suspend fun find(
        mongoUri: String,
        database: String,
        collection: String,
        limit: Int = 50
    ): List<RemoteSessionDto> = withContext(Dispatchers.IO) {
        if (mongoUri.isBlank()) return@withContext emptyList()
        runCatching {
            MongoClient.create(mongoUri).use { client ->
                client.getDatabase(database)
                    .getCollection<Document>(collection)
                    .find()
                    .limit(limit)
                    .toList()
                    .mapNotNull { documentToSession(it) }
            }
        }.getOrDefault(emptyList())
    }

    companion object {
        fun sessionToDocument(
            sessionId: String,
            mode: String,
            startedAt: Long,
            endedAt: Long,
            messages: List<Pair<String, String>>
        ): Document = Document().apply {
            append("sessionId", sessionId)
            append("mode", mode)
            append("startedAt", startedAt)
            append("endedAt", endedAt)
            append("messageCount", messages.size)
            append("messages", messages.map { (role, content) ->
                Document().apply {
                    append("role", role)
                    append("content", content)
                }
            })
        }

        fun documentToSession(doc: Document): RemoteSessionDto? = runCatching {
            val messagesRaw = doc.getList("messages", Document::class.java) ?: emptyList()
            RemoteSessionDto(
                sessionId = doc.getString("sessionId") ?: return@runCatching null,
                mode = doc.getString("mode") ?: "PHILOSOPHICAL",
                startedAt = doc.getLong("startedAt") ?: 0L,
                endedAt = doc.getLong("endedAt") ?: 0L,
                messageCount = doc.getInteger("messageCount") ?: messagesRaw.size,
                messages = messagesRaw.map { m ->
                    (m.getString("role") ?: "user") to (m.getString("content") ?: "")
                }
            )
        }.getOrNull()
    }
}

data class RemoteSessionDto(
    val sessionId: String,
    val mode: String,
    val startedAt: Long,
    val endedAt: Long,
    val messageCount: Int,
    val messages: List<Pair<String, String>>
)
