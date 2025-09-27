package com.example.smarthydro.chat.config

/**
 * Quick pre-check before sending user input to the AI.
 * This prevents unnecessary calls when a message is clearly out of scope.
 */
object ScopeGuard {

    /**
     * Returns true if the text seems related to plants/hydroponics.
     * (Just a keyword filter for speed; not security.)
     */
    fun isInScope(userInput: String): Boolean {
        val lower = userInput.lowercase()
        return AIAgentConfig.ALLOWED_KEYWORDS.any { keyword ->
            lower.contains(keyword)
        }
    }
}
