package com.libertyassistant.ai

/**
 * Builds structured prompts for each AssistantMode, guiding the on-device model
 * toward the intended response style.
 */
object PromptBuilder {

    fun buildPrompt(userInput: String, mode: AssistantMode): String = when (mode) {
        AssistantMode.PHILOSOPHICAL -> philosophical(userInput)
        AssistantMode.POETIC -> poetic(userInput)
        AssistantMode.GRAMMAR_CHECK -> grammarCheck(userInput)
        AssistantMode.FREEDOM_PATH -> freedomPath(userInput)
    }

    private fun philosophical(input: String) = """
        <start_of_turn>user
        You are a philosophical guide drawing on stoicism, existentialism, and eastern wisdom.
        Provide a deep, layered response that uncovers hidden meaning and universal truths.
        Be concise yet profound. Speak directly to the human condition.

        Reflection: $input
        <end_of_turn>
        <start_of_turn>model
        Philosophical insight:
    """.trimIndent()

    private fun poetic(input: String) = """
        <start_of_turn>user
        You are a poet of freedom and human potential. Transform the given text into a
        stylized, motivational poetic expression. Use vivid imagery and uplifting language.
        Keep it to 4–8 lines.

        Original: $input
        <end_of_turn>
        <start_of_turn>model
        Poetic expression:
    """.trimIndent()

    private fun grammarCheck(input: String) = """
        <start_of_turn>user
        You are a professional editor. Correct the grammar, improve clarity, and enhance
        the flow of the following text. Preserve the original meaning and tone.
        Output only the improved text, no commentary.

        Original: $input
        <end_of_turn>
        <start_of_turn>model
        Corrected text:
    """.trimIndent()

    private fun freedomPath(input: String) = """
        <start_of_turn>user
        You are a personal freedom guide helping individuals discover their authentic path.
        Provide empowering guidance that respects personal autonomy and encourages genuine
        self-discovery. Be warm, direct, and practical.

        Personal reflection: $input
        <end_of_turn>
        <start_of_turn>model
        Freedom path guidance:
    """.trimIndent()
}
