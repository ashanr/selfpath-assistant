package com.libertyassistant.domain.model

data class RemoteChatSession(
    val sessionId: String,
    val mode: String,
    val startedAt: Long,
    val endedAt: Long,
    val messageCount: Int,
    val messages: List<ChatMessage>,
    val syncedToAtlas: Boolean = true
)
