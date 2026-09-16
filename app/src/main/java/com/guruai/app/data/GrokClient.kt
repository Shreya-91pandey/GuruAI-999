package com.guruai.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GrokClient(private val apiKey: String) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .build()

    private val jsonType = "application/json; charset=utf-8".toMediaType()

    suspend fun chat(prompt: String, history: List<Pair<String, String>> = emptyList()): String {
        return withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) return@withContext "Grok API key missing. Settings mein key daalo."

            val messages = JSONArray()
            messages.put(
                JSONObject()
                    .put("role", "system")
                    .put("content", "You are Guru AI — a helpful, smart, friendly personal assistant. Reply in the same language the user uses (Hindi or English). Be concise and useful.")
            )

            history.takeLast(12).forEach { (role, text) ->
                val r = if (role == "user") "user" else "assistant"
                messages.put(JSONObject().put("role", r).put("content", text))
            }
            messages.put(JSONObject().put("role", "user").put("content", prompt))

            val body = JSONObject()
                .put("model", "grok-2-latest")
                .put("messages", messages)
                .put("stream", false)
                .toString()

            val request = Request.Builder()
                .url("https://api.x.ai/v1/chat/completions")
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(body.toRequestBody(jsonType))
                .build()

            client.newCall(request).execute().use { response ->
                val resp = response.body?.string() ?: ""
                if (!response.isSuccessful) return@withContext "Grok error ${response.code}: $resp"
                try {
                    JSONObject(resp)
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                        .trim()
                } catch (e: Exception) {
                    "Grok parse error: ${e.message}"
                }
            }
        }
    }
}
