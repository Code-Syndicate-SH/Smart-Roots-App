package com.example.smarthydro.chat

interface GeminiClient {
    suspend fun chat(messages: List<ChatMessage>): String
}

