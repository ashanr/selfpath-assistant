package com.libertyassistant.domain.usecase

import com.libertyassistant.ai.ApiInferenceEngine
import com.libertyassistant.ai.AssistantMode
import com.libertyassistant.ai.InferenceEngine
import com.libertyassistant.ai.PromptBuilder
import com.libertyassistant.data.preferences.UserPreferencesRepository
import com.libertyassistant.domain.model.AIResponse
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GenerateAIResponseUseCase @Inject constructor(
    private val inferenceEngine: InferenceEngine,
    private val apiInferenceEngine: ApiInferenceEngine,
    private val prefsRepository: UserPreferencesRepository
) {
    suspend operator fun invoke(input: String, mode: AssistantMode): AIResponse {
        return try {
            val prefs = prefsRepository.userPreferences.first()
            val response = if (prefs.useApi && prefs.apiKey.isNotBlank()) {
                apiInferenceEngine.generateResponse(input, mode, prefs)
            } else {
                val prompt = PromptBuilder.buildPrompt(input, mode)
                inferenceEngine.generateResponse(prompt)
            }
            AIResponse(content = response, mode = mode, inputPrompt = input)
        } catch (e: Exception) {
            AIResponse(
                content = "",
                mode = mode,
                inputPrompt = input,
                isError = true,
                errorMessage = e.message ?: "An unexpected error occurred"
            )
        }
    }
}
