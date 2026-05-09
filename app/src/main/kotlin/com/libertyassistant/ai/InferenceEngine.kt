package com.libertyassistant.ai

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps MediaPipe LLM Inference for fully offline, on-device text generation.
 * Gracefully degrades when no model is loaded.
 */
@Singleton
class InferenceEngine @Inject constructor(
    private val context: Context
) {
    private var llmInference: LlmInference? = null
    private var isInitialized = false
    private var initError: String? = null

    /**
     * Initializes the inference engine by extracting the model from assets and
     * loading it via MediaPipe LLM Inference. Safe to call multiple times.
     * Returns true on success, false if no model is available or loading fails.
     */
    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        if (isInitialized) return@withContext true

        val modelFile = ModelLoader.extractModelToCache(context)
        if (modelFile == null) {
            initError = "No model file found in assets/models/. " +
                "Place a Gemma-2B or Qwen-0.5B TFLite model and rebuild."
            return@withContext false
        }

        return@withContext try {
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelFile.absolutePath)
                .setMaxTokens(1024)
                .setTopK(40)
                .setTemperature(0.8f)
                .setRandomSeed(101)
                .build()

            llmInference = LlmInference.createFromOptions(context, options)
            isInitialized = true
            initError = null
            true
        } catch (e: Exception) {
            initError = "Model failed to load: ${e.message}"
            isInitialized = false
            false
        }
    }

    /**
     * Generates a response for the given prompt. Returns a fallback message
     * if the model is not loaded, avoiding a crash.
     */
    suspend fun generateResponse(prompt: String): String = withContext(Dispatchers.IO) {
        llmInference?.generateResponse(prompt) ?: FALLBACK_RESPONSE
    }

    fun isReady(): Boolean = isInitialized && llmInference != null

    fun getInitError(): String? = initError

    fun release() {
        llmInference?.close()
        llmInference = null
        isInitialized = false
    }

    companion object {
        private const val FALLBACK_RESPONSE =
            "The AI model is not loaded. Please place a compatible TFLite model " +
            "(Gemma-2B or Qwen-0.5B) in app/src/main/assets/models/ and rebuild."
    }
}
