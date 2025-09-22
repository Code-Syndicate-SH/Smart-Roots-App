package com.example.smarthydro.chat

import com.example.smarthydro.chat.tools.Tool

class FredAgent(
    private val llm: GeminiClient,
    private val tools: List<Tool>,
    private val systemInstructions: String =
        "You are Fred, an in-app assistant for SmartHydro. Be concise, use bullets, and provide actionable steps."
) {
    suspend fun reply(userText: String, history: List<ChatMessage>): String {
        tools.firstOrNull { it.matches(userText) }?.let { return it.invoke(userText).humanSummary }
        val messages = buildList {
            add(ChatMessage("system", systemInstructions))
            addAll(history)
            add(ChatMessage("user", userText))
        }
        return llm.chat("gemini-2.5-flash", messages)
    }
}
