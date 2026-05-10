package com.libertyassistant.domain.model

data class ChatMessage(
    val role: String,       // "user" or "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
