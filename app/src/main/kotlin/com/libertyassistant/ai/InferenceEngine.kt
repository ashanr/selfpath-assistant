package com.libertyassistant.ai

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * On-device inference stub for MVP.
 * MediaPipe LLM is incompatible with 16 KB page-size devices (Android 15+).
 * Use API mode via Settings → AI Backend instead.
 */
@Singleton
class InferenceEngine @Inject constructor(
    @Suppress("UNUSED_PARAMETER") context: Context
) {
    private var initError: String? = null

    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        initError = "On-device inference is not supported on this device. " +
            "Enable API mode in Settings → AI Backend and enter your API key."
        false
    }

    suspend fun generateResponse(@Suppress("UNUSED_PARAMETER") prompt: String): String =
        withContext(Dispatchers.IO) { FALLBACK_RESPONSE }

    fun isReady(): Boolean = false

    fun getInitError(): String? = initError

    fun release() { /* no-op */ }

    companion object {
        private const val FALLBACK_RESPONSE =
            "On-device AI is unavailable on this device. " +
            "Please enable API mode in Settings \u2192 AI Backend and enter your API key."
    }
}
