package com.example.smarthydro.chat

interface GeminiClient {
    suspend fun chat(model: String, messages: List<ChatMessage>): String
}
