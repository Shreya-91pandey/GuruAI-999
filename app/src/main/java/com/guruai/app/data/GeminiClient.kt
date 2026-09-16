package com.guruai.app.data

import android.content.ContentResolver
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiClient(private val apiKey: String) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .build()

    private val jsonType = "application/json; charset=utf-8".toMediaType()

    suspend fun chat(prompt: String, history: List<Pair<String, String>> = emptyList()): String {
        return withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) return@withContext "Gemini API key missing. Settings mein key daalo."

            val contents = JSONArray()
            history.takeLast(12).forEach { (role, text) ->
                val geminiRole = if (role == "user") "user" else "model"
                contents.put(
                    JSONObject()
                        .put("role", geminiRole)
                        .put("parts", JSONArray().put(JSONObject().put("text", text)))
                )
            }
            contents.put(
                JSONObject()
                    .put("role", "user")
                    .put("parts", JSONArray().put(JSONObject().put("text", prompt)))
            )

            val body = JSONObject().put("contents", contents).toString()
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
                .post(body.toRequestBody(jsonType))
                .build()

            client.newCall(request).execute().use { response ->
                val resp = response.body?.string() ?: ""
                if (!response.isSuccessful) return@withContext "Gemini error ${response.code}: $resp"
                try {
                    val json = JSONObject(resp)
                    json.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")
                        .trim()
                } catch (e: Exception) {
                    "Gemini parse error: ${e.message}"
                }
            }
        }
    }

    suspend fun analyzeImage(
        contentResolver: ContentResolver,
        uri: Uri,
        prompt: String
    ): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext "Gemini API key missing."

        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: return@withContext "Could not read image."

        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        val mime = contentResolver.getType(uri) ?: "image/jpeg"

        val parts = JSONArray()
            .put(JSONObject().put("text", prompt))
            .put(
                JSONObject().put(
                    "inline_data",
                    JSONObject()
                        .put("mime_type", mime)
                        .put("data", base64)
                )
            )

        val body = JSONObject()
            .put("contents", JSONArray().put(JSONObject().put("parts", parts)))
            .toString()

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
            .post(body.toRequestBody(jsonType))
            .build()

        client.newCall(request).execute().use { response ->
            val resp = response.body?.string() ?: ""
            if (!response.isSuccessful) return@withContext "Gemini vision error ${response.code}"
            try {
                JSONObject(resp)
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                    .trim()
            } catch (e: Exception) {
                "Could not analyze image: ${e.message}"
            }
        }
    }
}
