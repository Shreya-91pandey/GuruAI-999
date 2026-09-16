package com.guruai.app.data

data class ChatMessage(
    val id: String = System.currentTimeMillis().toString(),
    val content: String,
    val isUser: Boolean,
    val model: String = "",          // "Gemini" or "Grok"
    val timestamp: Long = System.currentTimeMillis()
)

enum class AiModel(val displayName: String) {
    GEMINI("Gemini"),
    GROK("Grok")
}
