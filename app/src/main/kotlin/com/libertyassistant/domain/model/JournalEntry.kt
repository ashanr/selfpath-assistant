package com.libertyassistant.domain.model

data class JournalEntry(
    val id: Long = 0,
    val title: String,
    val content: String,
    val mode: String,
    val aiResponse: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
