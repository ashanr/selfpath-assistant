package com.libertyassistant.ai

/**
 * Defines the available AI assistant modes with their display names and descriptions.
 */
enum class AssistantMode(val displayName: String, val description: String) {
    PHILOSOPHICAL(
        displayName = "Philosophical",
        description = "Deep, layered philosophical insights rooted in stoicism and eastern wisdom"
    ),
    POETIC(
        displayName = "Poetic",
        description = "Stylized, motivational poetic expression that uplifts the spirit"
    ),
    GRAMMAR_CHECK(
        displayName = "Grammar Check",
        description = "Grammar correction, clarity improvement, and text enhancement"
    ),
    FREEDOM_PATH(
        displayName = "Freedom Path",
        description = "Personal freedom and authentic self-discovery guidance"
    );
}
