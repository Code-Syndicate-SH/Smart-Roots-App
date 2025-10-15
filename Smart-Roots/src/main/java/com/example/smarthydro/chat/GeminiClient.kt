package com.example.smarthydro.chat

interface GeminiClient {
    suspend fun chat(messages: List<ChatMessage>): String

    /** Ask a question about an image (bytes + mime). History is optional. */
    suspend fun chatImage(
        question: String,
        imageBytes: ByteArray,
        mimeType: String,
        history: List<ChatMessage> = emptyList()
    ): String
}
