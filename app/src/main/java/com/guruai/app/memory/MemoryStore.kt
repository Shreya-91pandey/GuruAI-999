package com.guruai.app.memory

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class StoredMessage(
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

class MemoryStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("guru_memory", Context.MODE_PRIVATE)

    fun saveMessage(role: String, content: String) {
        val list = getAllMessages().toMutableList()
        list.add(StoredMessage(role, content))
        // Keep last 200 messages
        val trimmed = if (list.size > 200) list.takeLast(200) else list
        saveAll(trimmed)
    }

    fun getAllMessages(): List<StoredMessage> {
        val json = prefs.getString("messages", "[]") ?: "[]"
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                StoredMessage(
                    role = obj.getString("role"),
                    content = obj.getString("content"),
                    timestamp = obj.optLong("timestamp", 0L)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getMessagesGroupedByDate(): Map<String, List<StoredMessage>> {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return getAllMessages()
            .groupBy { sdf.format(Date(it.timestamp)) }
            .toSortedMap(compareByDescending { it })
    }

    fun clearAll() {
        prefs.edit().putString("messages", "[]").apply()
    }

    private fun saveAll(list: List<StoredMessage>) {
        val arr = JSONArray()
        list.forEach { msg ->
            arr.put(
                JSONObject()
                    .put("role", msg.role)
                    .put("content", msg.content)
                    .put("timestamp", msg.timestamp)
            )
        }
        prefs.edit().putString("messages", arr.toString()).apply()
    }
}
