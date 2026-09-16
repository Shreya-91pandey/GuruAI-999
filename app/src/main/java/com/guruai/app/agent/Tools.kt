package com.guruai.app.agent

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

interface Tool {
    val name: String
    val description: String
    suspend fun execute(input: String): String
}

class GetTimeTool : Tool {
    override val name = "get_time"
    override val description = "Get current date and time"

    override suspend fun execute(input: String): String {
        val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy, hh:mm a", Locale.getDefault())
        return "Current time: ${sdf.format(Date())}"
    }
}

class WebSearchTool(
    private val apiKey: String,
    private val cx: String
) : Tool {
    override val name = "web_search"
    override val description = "Search the web for current information"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    override suspend fun execute(input: String): String {
        if (apiKey.isBlank() || cx.isBlank()) {
            return "Web search is not configured. Add Google Custom Search API key + CX in Settings."
        }
        return try {
            val url = "https://www.googleapis.com/customsearch/v1?key=$apiKey&cx=$cx&q=${java.net.URLEncoder.encode(input, "UTF-8")}&num=5"
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: return "Search failed"
                val json = JSONObject(body)
                val items = json.optJSONArray("items") ?: return "No results found"
                val results = (0 until items.length()).joinToString("\n\n") { i ->
                    val item = items.getJSONObject(i)
                    "• ${item.optString("title")}\n  ${item.optString("snippet")}\n  ${item.optString("link")}"
                }
                "Search results for \"$input\":\n\n$results"
            }
        } catch (e: Exception) {
            "Search error: ${e.message}"
        }
    }
}

class ToolRegistry {
    private val tools = mutableMapOf<String, Tool>()

    fun register(tool: Tool) {
        tools[tool.name] = tool
    }

    fun get(name: String): Tool? = tools[name]

    fun all(): List<Tool> = tools.values.toList()
}

class AgentLoop(
    private val llmClient: suspend (String) -> String,
    private val toolRegistry: ToolRegistry
) {
    suspend fun run(userQuery: String): String {
        // Simple tool detection
        val lower = userQuery.lowercase()
        when {
            lower.contains("time") || lower.contains("समय") || lower.contains("kitna baja") -> {
                val tool = toolRegistry.get("get_time")
                return tool?.execute("") ?: llmClient(userQuery)
            }
            lower.contains("search") || lower.contains("खोज") || lower.contains("google") ||
                lower.contains("net pe") || lower.contains("pata karo") || lower.contains("check kar") -> {
                val tool = toolRegistry.get("web_search")
                if (tool != null) {
                    val result = tool.execute(userQuery)
                    // Ask LLM to summarize the search results
                    return llmClient("User asked: $userQuery\n\nHere are search results:\n$result\n\nGive a clear, helpful answer based on these results.")
                }
            }
        }
        return llmClient(userQuery)
    }
}
