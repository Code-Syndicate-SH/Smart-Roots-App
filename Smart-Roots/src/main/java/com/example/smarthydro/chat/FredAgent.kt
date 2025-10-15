package com.example.smarthydro.chat

import com.example.smarthydro.chat.config.AIAgentConfig
import com.example.smarthydro.chat.tools.Tool

class FredAgent(
    private val llm: GeminiClient,
    private val tools: List<Tool> = emptyList(),
) {

    // 🧠 Text-based reply
    suspend fun reply(userText: String, history: List<ChatMessage>): String {
        // 1️⃣ Check if a tool can handle this query first
        tools.firstOrNull { it.matches(userText) }?.let { tool ->
            return tool.invoke(userText).humanSummary
        }

        // 2️⃣ Build the conversation (system prompt + chat history + new message)
        val convo = buildList {
            add(ChatMessage("system", AIAgentConfig.SYSTEM_PROMPT))
            addAll(history)
            add(ChatMessage("user", userText))
        }

        // 3️⃣ Call Gemini (text model)
        return runCatching {
            llm.chat(convo)
        }.getOrElse { th ->
            "Sorry, something went wrong: ${th.localizedMessage ?: th.javaClass.simpleName}"
        }
    }

    // 📸 Image-based reply (photo + question)
    suspend fun replyWithImage(
        question: String,
        imageBytes: ByteArray,
        mimeType: String,
        history: List<ChatMessage>
    ): String {
        return runCatching {
            llm.chatImage(
                question = question,
                imageBytes = imageBytes,
                mimeType = mimeType,
                history = history
            )
        }.getOrElse { th ->
            "Sorry, something went wrong while analyzing the image: ${th.localizedMessage ?: th.javaClass.simpleName}"
        }
    }
}
