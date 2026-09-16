package com.guruai.app.data

import android.content.Context
import com.guruai.app.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Unified AI caller with Gemini + Grok fallback
 */
class AiRepository(private val context: Context) {

    private val prefs = Prefs(context)

    suspend fun chat(prompt: String, history: List<Pair<String, String>> = emptyList()): String {
        return withContext(Dispatchers.IO) {
            val providers = mutableListOf<Pair<String, suspend () -> String>>()

            if (prefs.geminiKey.isNotBlank()) {
                providers.add("Gemini" to {
                    GeminiClient(prefs.geminiKey).chat(prompt, history)
                })
            }
            if (prefs.grokKey.isNotBlank()) {
                providers.add("Grok" to {
                    GrokClient(prefs.grokKey).chat(prompt, history)
                })
            }

            if (providers.isEmpty()) {
                return@withContext "Koi API key nahi mili. Settings mein Gemini ya Grok key daalo."
            }

            val preferred = when (prefs.aiProvider) {
                Constants.PROVIDER_GROK -> "Grok"
                else -> "Gemini"
            }
            val ordered = providers.sortedByDescending { it.first == preferred }

            var lastError = "Could not reach any AI provider."
            for ((name, call) in ordered) {
                val result = try {
                    call()
                } catch (e: Exception) {
                    "error: ${e.message}"
                }

                val looksLikeFailure =
                    result.startsWith("Gemini error") ||
                        result.startsWith("Grok error") ||
                        result.startsWith("error:") ||
                        result.contains("API key missing") ||
                        result.contains("key missing")

                if (!looksLikeFailure) {
                    return@withContext result
                }
                lastError = result
            }
            lastError
        }
    }

    fun friendlyReply(raw: String): String {
        val lower = raw.lowercase()
        val looksLikeError =
            lower.contains("exception") ||
                lower.contains("failed to connect") ||
                lower.contains("unable to resolve host") ||
                lower.contains("timeout") ||
                lower.contains("no ai provider") ||
                raw.startsWith("error:") ||
                raw.startsWith("Gemini error") ||
                raw.startsWith("Grok error") ||
                raw.startsWith("Could not reach")

        return if (looksLikeError) {
            "Lagta hai internet connection weak hai ya AI thoda busy hai. Thodi der baad try karo. 🙏"
        } else {
            raw
        }
    }
}
