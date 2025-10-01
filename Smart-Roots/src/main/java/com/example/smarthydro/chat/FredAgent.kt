package com.example.smarthydro.chat

import com.example.smarthydro.chat.config.AIAgentConfig
import com.example.smarthydro.chat.tools.Tool

class FredAgent(
    private val llm: GeminiClient,
    private val tools: List<Tool> = emptyList(),
) {
    suspend fun reply(userText: String, history: List<ChatMessage>): String {
        // tools first (optional)
        tools.firstOrNull { it.matches(userText) }?.let { tool ->
            return tool.invoke(userText).humanSummary
        }

        val convo = buildList {
            add(ChatMessage("system", AIAgentConfig.SYSTEM_PROMPT))
            addAll(history)
            add(ChatMessage("user", userText))
        }
        return llm.chat(convo)
    }
}
