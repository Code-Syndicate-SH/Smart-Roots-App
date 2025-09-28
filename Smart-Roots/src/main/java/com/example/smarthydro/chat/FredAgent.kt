package com.example.smarthydro.chat

import com.example.smarthydro.chat.tools.Tool
import com.example.smarthydro.chat.tools.SayToUser
import com.example.smarthydro.chat.tools.AskUser
import com.example.smarthydro.chat.config.AIAgentConfig
import com.example.smarthydro.chat.config.ScopeGuard

class FredAgent(
    private val llm: GeminiClient,
    private val tools: List<Tool>,
    private val systemInstructions: String =
        "You are Fred, an in-app assistant for SmartHydro. Be concise, use bullets, and provide actionable advice."
) {
    suspend fun reply(userText: String, history: List<ChatMessage>): String {
        // 🔒 Scope check
        if (!ScopeGuard.isInScope(userText)) {
            SayToUser.send(AIAgentConfig.OUT_OF_SCOPE_MESSAGE)
            return AIAgentConfig.OUT_OF_SCOPE_MESSAGE
        }

        // 🔧 Tool handling
        tools.firstOrNull { it.matches(userText) }?.let {
            val toolResponse = it.invoke(userText).humanSummary
            SayToUser.send(toolResponse)
            return toolResponse
        }

        // 🔧 Build conversation messages
        val messages = buildList {
            add(ChatMessage(role = "system", content = systemInstructions))
            addAll(history)
            add(ChatMessage(role = "user", content = userText))
        }

        // 🔧 Call AI
        val response = llm.chat(messages)

        // 🔧 Always send the response to the user
        SayToUser.send(response)

        // 🔧 If the response is a clarifying question, ask user
        if (response.trim().endsWith("?")) {
            AskUser.ask(response)
        }

        return response
    }
}
