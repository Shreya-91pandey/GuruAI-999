package com.guruai.app.util

object Constants {
    const val PROVIDER_GEMINI = "gemini"
    const val PROVIDER_GROK = "grok"
    const val DEVICE_MODEL = "Guru AI"

    data class ThemeColors(
        val name: String,
        val background: String,
        val surface: String,
        val accent: String,
        val textPrimary: String,
        val textSecondary: String
    )

    val THEMES = listOf(
        ThemeColors(
            name = "Midnight Gold",
            background = "#0A0A0A",
            surface = "#121212",
            accent = "#F5C518",
            textPrimary = "#F5F5F5",
            textSecondary = "#AAAAAA"
        ),
        ThemeColors(
            name = "Deep Purple",
            background = "#0D0D0D",
            surface = "#1A1A1A",
            accent = "#7C4DFF",
            textPrimary = "#E8E8E8",
            textSecondary = "#999999"
        ),
        ThemeColors(
            name = "Ocean",
            background = "#0A1218",
            surface = "#12202A",
            accent = "#00BCD4",
            textPrimary = "#E0F7FA",
            textSecondary = "#80CBC4"
        ),
        ThemeColors(
            name = "Forest",
            background = "#0A120A",
            surface = "#121A12",
            accent = "#4CAF50",
            textPrimary = "#E8F5E9",
            textSecondary = "#A5D6A7"
        )
    )
}
