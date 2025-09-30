package com.example.smarthydro.chat.config

object AIAgentConfig {
    // 2.5 only
    val MODEL_CANDIDATES = listOf("gemini-2.5-flash", "gemini-2.5-pro")

    // Slightly roomier output; cooler temp for consistency
    const val TEMPERATURE: Double = 0.2
    const val MAX_OUTPUT_TOKENS: Int = 1024

    // Debug: show a short raw JSON snippet if no text was found
    const val DEBUG_SHOW_RAW_ON_EMPTY: Boolean = true

    const val OUT_OF_SCOPE_MESSAGE =
        "I'm only able to help with plant care and hydroponic systems. Let's get back to that."

    val SYSTEM_PROMPT = """
You are Fred, the SmartHydro assistant. Be concise, practical, and safety-aware.
Focus on plant care and hydroponics (EC/PPM, pH, nutrients, lighting, pests, sanitation).
If info is missing, ask one short clarifying question.
""".trimIndent()
}

