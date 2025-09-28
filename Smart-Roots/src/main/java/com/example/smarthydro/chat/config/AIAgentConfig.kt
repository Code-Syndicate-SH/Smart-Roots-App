package com.example.smarthydro.chat.config

/**
 * Central configuration for the plant/hydroponics agent.
 * This file does not depend on your existing agent classes,
 * so it’s safe to add without touching current code.
 */
object AIAgentConfig {

    // Model & generation settings (keep responses stable and deterministic-ish)
    const val MODEL_NAME: String = "gemini-1.5-pro" // or whatever your Gemini/OpenAI model resolver uses
    const val TEMPERATURE: Float = 0.1f
    const val TOP_P: Float = 0.8f
    const val TOP_K: Int = 40
    const val MAX_OUTPUT_TOKENS: Int = 512

    /**
     * Strict system prompt: the agent must only operate within
     * plant care & hydroponic topics. Anything else must be declined.
     */
    val SYSTEM_PROMPT: String = """
You are the **Smart Roots Plant Care Agent**.

SCOPE:
- You may answer ONLY questions about plant care and hydroponic systems, including:
  seeds, germination, growth stages, pests/diseases, pruning, nutrients and mixing, EC/PPM, pH, reservoirs,
  pumps, irrigation types (DWC/NFT/drip/aeroponics), substrates (coco, perlite, rockwool), lighting (PAR/PPFD/LED),
  ventilation, temperature, humidity, CO₂, greenhouses/grow tents, sensors, water quality, sanitation, and safety.
- If the user asks anything OUTSIDE this scope (e.g., politics, sports, programming, personal advice, math homework, etc.),
  you MUST NOT answer. Instead, respond with the short closing line below and stop.

STYLE:
- Be practical, concise, and safety-aware (electrical, chemical, food safety).
- Use clear units and ranges (e.g., °C/°F, EC/PPM, pH, mL/L).
- If data is missing, ask ONE short, targeted question via AskUser.
- Do not fabricate measurements or guarantees.

CLOSING LINE FOR OUT-OF-SCOPE:
"I'm only able to help with plant care and hydroponic systems. Let's get back to that."
""".trimIndent()

    /** Single, consistent out-of-scope message to keep the UX predictable. */
    const val OUT_OF_SCOPE_MESSAGE: String =
        "I'm only able to help with plant care and hydroponic systems. Let's get back to that."

    /**
     * A lightweight allowlist to help a pre-check (we’ll use it in the ScopeGuard).
     * This is NOT security—just a fast, local filter before we even call the model.
     */
    val ALLOWED_KEYWORDS: Set<String> = setOf(
        "plant", "plants", "leaf", "leaves", "root", "roots", "seed", "germination",
        "hydroponic", "hydroponics", "dwc", "nft", "aeroponics", "ebb", "flow",
        "nutrient", "nutrients", "fertilizer", "solution", "reservoir", "ppm", "ec", "pH",
        "pump", "irrigation", "drip", "drain", "substrate", "coco", "perlite", "rockwool",
        "grow light", "light", "led", "par", "ppfd", "photoperiod",
        "humidity", "temperature", "ventilation", "fan", "co2", "greenhouse", "tent",
        "pest", "disease", "aphid", "mite", "mildew", "botrytis", "algae",
        "sanitation", "flush", "water quality", "phosphorus", "nitrogen", "potassium", "cal-mag"
    )
}
