package com.guruai.app.data

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("guru_ai_prefs", Context.MODE_PRIVATE)

    var geminiKey: String
        get() = prefs.getString("gemini_key", "") ?: ""
        set(value) = prefs.edit().putString("gemini_key", value.trim()).apply()

    var grokKey: String
        get() = prefs.getString("grok_key", "") ?: ""
        set(value) = prefs.edit().putString("grok_key", value.trim()).apply()

    var aiProvider: String
        get() = prefs.getString("ai_provider", "gemini") ?: "gemini"
        set(value) = prefs.edit().putString("ai_provider", value).apply()

    var aiOnlineMode: Boolean
        get() = prefs.getBoolean("ai_online_mode", true)
        set(value) = prefs.edit().putBoolean("ai_online_mode", value).apply()

    var themeIndex: Int
        get() = prefs.getInt("theme_index", 0)
        set(value) = prefs.edit().putInt("theme_index", value).apply()

    var screenMonitorEnabled: Boolean
        get() = prefs.getBoolean("screen_monitor", false)
        set(value) = prefs.edit().putBoolean("screen_monitor", value).apply()

    var whatsappSyncEnabled: Boolean
        get() = prefs.getBoolean("whatsapp_sync", false)
        set(value) = prefs.edit().putBoolean("whatsapp_sync", value).apply()

    var emailSyncEnabled: Boolean
        get() = prefs.getBoolean("email_sync", false)
        set(value) = prefs.edit().putBoolean("email_sync", value).apply()

    var searchApiKey: String
        get() = prefs.getString("search_api_key", "") ?: ""
        set(value) = prefs.edit().putString("search_api_key", value.trim()).apply()

    var searchCx: String
        get() = prefs.getString("search_cx", "") ?: ""
        set(value) = prefs.edit().putString("search_cx", value.trim()).apply()

    var offlineModelPath: String
        get() = prefs.getString("offline_model_path", "") ?: ""
        set(value) = prefs.edit().putString("offline_model_path", value).apply()

    var conversationSummary: String
        get() = prefs.getString("conversation_summary", "") ?: ""
        set(value) = prefs.edit().putString("conversation_summary", value).apply()

    var summarizedUpToCount: Int
        get() = prefs.getInt("summarized_up_to", 0)
        set(value) = prefs.edit().putInt("summarized_up_to", value).apply()

    var ttsEnabled: Boolean
        get() = prefs.getBoolean("tts_enabled", true)
        set(value) = prefs.edit().putBoolean("tts_enabled", value).apply()
}
