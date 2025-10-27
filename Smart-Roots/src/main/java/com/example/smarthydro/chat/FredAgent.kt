package com.example.smarthydro.chat

import com.example.smarthydro.chat.config.AIAgentConfig
import com.example.smarthydro.chat.tools.Tool

class FredAgent(
    private val llm: GeminiClient,
    private val tools: List<Tool> = emptyList(),
) {

    // 🧠 Text-based reply
    suspend fun reply(userText: String, history: List<ChatMessage>): String {
        // 1️⃣ Try to resolve using tools first
        tools.firstOrNull { it.matches(userText) }?.let { tool ->
            return tool.invoke(userText).humanSummary.cleanResponse()
        }

        // 2️⃣ Build the conversation context
        val convo = buildList {
            add(ChatMessage("system", AIAgentConfig.SYSTEM_PROMPT))
            addAll(history)
            add(ChatMessage("user", userText))
        }

        // 3️⃣ Get model response
        val raw = runCatching {
            llm.chat(convo)
        }.getOrElse { th ->
            "Sorry, something went wrong: ${th.localizedMessage ?: th.javaClass.simpleName}"
        }

        return raw.cleanResponse()
    }

    // 📸 Image-based reply (photo + question)
    suspend fun replyWithImage(
        question: String,
        imageBytes: ByteArray,
        mimeType: String,
        history: List<ChatMessage>
    ): String {
        val raw = runCatching {
            llm.chatImage(
                question = question,
                imageBytes = imageBytes,
                mimeType = mimeType,
                history = history
            )
        }.getOrElse { th ->
            "Sorry, something went wrong while analyzing the image: ${th.localizedMessage ?: th.javaClass.simpleName}"
        }

        return raw.cleanResponse()
    }

    // ✨ Utility: Clean & format Gemini's markdown output
    private fun String.cleanResponse(): String {
        var text = this.trim()

        // Remove markdown markers
        text = text
            .replace("**", "")
            .replace("*", "")
            .replace("#", "")
            .replace("_", "")
            .replace("`", "")

        // Convert hyphens/numbers to clean bullets
        text = text.replace(Regex("(?m)^(\\d+\\.|[-–])\\s*"), "• ")

        // Fix double newlines and trim
        text = text.replace(Regex("\\n{3,}"), "\n\n").trim()

        return text
    }
}
