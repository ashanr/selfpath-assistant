package com.libertyassistant.ai

enum class ApiProvider(
    val displayName: String,
    val baseUrl: String,
    val defaultModel: String
) {
    OPENAI(
        displayName = "OpenAI",
        baseUrl = "https://api.openai.com/v1",
        defaultModel = "gpt-4o-mini"
    ),
    OPENROUTER(
        displayName = "OpenRouter",
        baseUrl = "https://openrouter.ai/api/v1",
        defaultModel = "openai/gpt-4o-mini"
    )
}
