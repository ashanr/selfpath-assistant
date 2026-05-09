package com.libertyassistant.domain.model

import com.libertyassistant.ai.AssistantMode

data class AIResponse(
    val content: String,
    val mode: AssistantMode,
    val inputPrompt: String,
    val isError: Boolean = false,
    val errorMessage: String? = null
)
