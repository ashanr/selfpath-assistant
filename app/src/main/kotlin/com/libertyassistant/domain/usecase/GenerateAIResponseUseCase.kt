package com.libertyassistant.domain.usecase

import com.libertyassistant.ai.AssistantMode
import com.libertyassistant.ai.InferenceEngine
import com.libertyassistant.ai.PromptBuilder
import com.libertyassistant.domain.model.AIResponse
import javax.inject.Inject

class GenerateAIResponseUseCase @Inject constructor(
    private val inferenceEngine: InferenceEngine
) {
    suspend operator fun invoke(input: String, mode: AssistantMode): AIResponse {
        return try {
            val prompt = PromptBuilder.buildPrompt(input, mode)
            val response = inferenceEngine.generateResponse(prompt)
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
