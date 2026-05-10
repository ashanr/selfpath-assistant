package com.libertyassistant.ai

import com.libertyassistant.data.preferences.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Calls an OpenAI-compatible chat completions endpoint (OpenAI or OpenRouter).
 * Uses HttpURLConnection to avoid additional library dependencies.
 */
@Singleton
class ApiInferenceEngine @Inject constructor() {

    suspend fun generateResponse(
        input: String,
        mode: AssistantMode,
        prefs: UserPreferences
    ): String = withContext(Dispatchers.IO) {
        val provider = prefs.apiProvider
        val model = prefs.apiModel.ifBlank { provider.defaultModel }
        val url = URL("${provider.baseUrl}/chat/completions")

        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Bearer ${prefs.apiKey}")
            if (provider == ApiProvider.OPENROUTER) {
                connection.setRequestProperty(
                    "HTTP-Referer",
                    "https://github.com/ashanr/selfpath-assistant"
                )
                connection.setRequestProperty("X-Title", "SelfPath Assistant")
            }
            connection.doOutput = true
            connection.connectTimeout = 30_000
            connection.readTimeout = 60_000

            val body = JSONObject().apply {
                put("model", model)
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemPromptFor(mode))
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", input)
                    })
                })
                put("max_tokens", 1024)
            }.toString()

            OutputStreamWriter(connection.outputStream).use { it.write(body) }

            val responseCode = connection.responseCode
            val responseStream =
                if (responseCode in 200..299) connection.inputStream else connection.errorStream
            val responseText = responseStream.bufferedReader().readText()

            if (responseCode !in 200..299) {
                throw Exception("API error $responseCode: $responseText")
            }

            JSONObject(responseText)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim()
        } finally {
            connection.disconnect()
        }
    }

    private fun systemPromptFor(mode: AssistantMode): String = when (mode) {
        AssistantMode.PHILOSOPHICAL ->
            "You are a philosophical guide drawing on stoicism, existentialism, and eastern wisdom. " +
            "Provide deep, layered responses that uncover hidden meaning and universal truths. " +
            "Be concise yet profound. Speak directly to the human condition."
        AssistantMode.POETIC ->
            "You are a poet of freedom and human potential. Transform the given text into a " +
            "stylized, motivational poetic expression. Use vivid imagery and uplifting language. " +
            "Keep it to 4–8 lines."
        AssistantMode.GRAMMAR_CHECK ->
            "You are a professional editor. Correct grammar, improve clarity, and enhance flow. " +
            "Preserve the original meaning and tone. Output only the improved text, no commentary."
        AssistantMode.FREEDOM_PATH ->
            "You are a personal freedom guide helping individuals discover their authentic path. " +
            "Provide empowering guidance that respects personal autonomy and encourages genuine " +
            "self-discovery. Be warm, direct, and practical."
    }
}
