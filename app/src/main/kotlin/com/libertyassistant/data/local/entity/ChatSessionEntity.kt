package com.libertyassistant.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey val id: String,
    val mode: String,
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    val messageCount: Int = 0,
    val syncedToAtlas: Boolean = false
)
