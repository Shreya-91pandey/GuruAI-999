package com.guruai.app.memory

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class Note(
    val id: String = System.currentTimeMillis().toString(),
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

class KnowledgeStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("guru_knowledge", Context.MODE_PRIVATE)

    fun save(title: String, content: String) {
        val list = getAll().toMutableList()
        list.add(0, Note(title = title, content = content))
        // Keep last 100 notes
        val trimmed = if (list.size > 100) list.take(100) else list
        saveAll(trimmed)
    }

    fun getAll(): List<Note> {
        val json = prefs.getString("notes", "[]") ?: "[]"
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                Note(
                    id = obj.optString("id", ""),
                    title = obj.getString("title"),
                    content = obj.getString("content"),
                    timestamp = obj.optLong("timestamp", 0L)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun findRelevant(query: String, limit: Int = 3): List<Note> {
        val lower = query.lowercase()
        return getAll()
            .filter {
                it.title.lowercase().contains(lower) ||
                    it.content.lowercase().contains(lower) ||
                    lower.split(" ").any { word ->
                        word.length > 3 && (it.title.lowercase().contains(word) || it.content.lowercase().contains(word))
                    }
            }
            .take(limit)
    }

    fun clearAll() {
        prefs.edit().putString("notes", "[]").apply()
    }

    private fun saveAll(list: List<Note>) {
        val arr = JSONArray()
        list.forEach { note ->
            arr.put(
                JSONObject()
                    .put("id", note.id)
                    .put("title", note.title)
                    .put("content", note.content)
                    .put("timestamp", note.timestamp)
            )
        }
        prefs.edit().putString("notes", arr.toString()).apply()
    }
}
