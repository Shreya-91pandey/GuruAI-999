package com.guruai.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.guruai.app.agent.AgentLoop
import com.guruai.app.agent.GetTimeTool
import com.guruai.app.agent.ToolRegistry
import com.guruai.app.agent.WebSearchTool
import com.guruai.app.data.AiRepository
import com.guruai.app.data.ChatMessage
import com.guruai.app.data.Prefs
import com.guruai.app.memory.KnowledgeStore
import com.guruai.app.memory.MemoryStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val statusText: String = "",
    val geminiKeyPresent: Boolean = false,
    val grokKeyPresent: Boolean = false,
    val onlineMode: Boolean = true
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = Prefs(application)
    private val memoryStore = MemoryStore(application)
    private val knowledgeStore = KnowledgeStore(application)
    private val aiRepo = AiRepository(application)

    private val toolRegistry = ToolRegistry().apply {
        register(GetTimeTool())
        register(WebSearchTool(prefs.searchApiKey, prefs.searchCx))
    }

    private val agentLoop = AgentLoop(
        llmClient = { prompt -> aiRepo.chat(prompt) },
        toolRegistry = toolRegistry
    )

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val historyPairs = mutableListOf<Pair<String, String>>()

    init {
        loadHistory()
        refreshStatus()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            val saved = memoryStore.getAllMessages()
            if (saved.isNotEmpty()) {
                val msgs = saved.map {
                    ChatMessage(
                        content = it.content,
                        isUser = it.role == "user",
                        model = if (it.role == "user") "" else "Guru"
                    )
                }
                historyPairs.clear()
                saved.forEach { historyPairs.add(it.role to it.content) }
                _uiState.value = _uiState.value.copy(messages = msgs)
            } else {
                val welcome = ChatMessage(
                    content = "Hey! Main Guru AI hoon. Settings mein Gemini ya Grok key daalo, phir baat shuru karo. ЁЯЩП",
                    isUser = false,
                    model = "Guru"
                )
                _uiState.value = _uiState.value.copy(messages = listOf(welcome))
            }
        }
    }

    fun refreshStatus() {
        val geminiOk = prefs.geminiKey.isNotBlank()
        val grokOk = prefs.grokKey.isNotBlank()
        val mode = if (prefs.aiOnlineMode) "Online" else "Offline"
        val keyStatus = when {
            geminiOk && grokOk -> "Gemini + Grok OK"
            geminiOk -> "Gemini OK"
            grokOk -> "Grok OK"
            else -> "API key add karo"
        }
        _uiState.value = _uiState.value.copy(
            statusText = "$keyStatus ┬╖ $mode",
            geminiKeyPresent = geminiOk,
            grokKeyPresent = grokOk,
            onlineMode = prefs.aiOnlineMode
        )
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMsg = ChatMessage(content = text.trim(), isUser = true)
        historyPairs.add("user" to text.trim())
        memoryStore.saveMessage("user", text.trim())

        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMsg,
            isLoading = true,
            error = null
        )

        if (!prefs.aiOnlineMode) {
            appendAssistant("AI Online Mode band hai. Settings mein on karo.")
            return
        }

        viewModelScope.launch {
            val lower = text.lowercase()
            val needsTool = listOf("time", "рд╕рдордп", "search", "рдЦреЛрдЬ", "google", "net pe", "pata karo", "check kar")
                .any { lower.contains(it) }

            val relevantNotes = knowledgeStore.findRelevant(text).map { it.content }
            val prompt = buildPrompt(text.trim(), relevantNotes)

            val raw = if (needsTool) {
                agentLoop.run(text.trim())
            } else {
                aiRepo.chat(prompt, historyPairs.dropLast(1).takeLast(10))
            }

            val reply = aiRepo.friendlyReply(raw)
            appendAssistant(reply)
        }
    }

    private fun buildPrompt(userMessage: String, notes: List<String>): String {
        return buildString {
            if (notes.isNotEmpty()) {
                append("Relevant notes:\n")
                notes.forEach { append("- $it\n") }
                append("\n")
            }
            val summary = prefs.conversationSummary
            if (summary.isNotBlank()) {
                append("Earlier summary:\n$summary\n\n")
            }
            append("User: $userMessage")
        }
    }

    private fun appendAssistant(text: String) {
        val aiMsg = ChatMessage(content = text, isUser = false, model = "Guru")
        historyPairs.add("assistant" to text)
        memoryStore.saveMessage("assistant", text)

        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + aiMsg,
            isLoading = false
        )

        viewModelScope.launch { maybeSummarize() }
    }

    private suspend fun maybeSummarize() {
        val newCount = historyPairs.size - prefs.summarizedUpToCount
        if (newCount < 20 || !prefs.aiOnlineMode) return

        val toSummarize = historyPairs.drop(prefs.summarizedUpToCount).dropLast(4)
        if (toSummarize.isEmpty()) return

        val transcript = toSummarize.joinToString("\n") { (role, text) ->
            "${if (role == "user") "User" else "Guru"}: $text"
        }
        val existing = prefs.conversationSummary
        val prompt = if (existing.isBlank()) {
            "Summarize this conversation briefly, keep key facts:\n\n$transcript"
        } else {
            "Existing summary:\n$existing\n\nNew messages:\n$transcript\n\nGive one updated short summary."
        }
        val summary = aiRepo.chat(prompt)
        if (!summary.startsWith("Gemini error") && !summary.startsWith("Grok error")) {
            prefs.conversationSummary = summary
            prefs.summarizedUpToCount = historyPairs.size - 4
        }
    }

    fun clearChat() {
        historyPairs.clear()
        memoryStore.clearAll()
        prefs.conversationSummary = ""
        prefs.summarizedUpToCount = 0
        val welcome = ChatMessage(
            content = "Chat cleared. Main Guru AI hoon тАФ sawal poochho!",
            isUser = false,
            model = "Guru"
        )
        _uiState.value = _uiState.value.copy(messages = listOf(welcome))
    }

    fun saveNote(text: String) {
        if (text.isBlank()) return
        knowledgeStore.save(title = text.take(40), content = text)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
