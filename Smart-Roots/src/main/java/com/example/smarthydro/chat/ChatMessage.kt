package com.example.smarthydro.chat

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val role: String,   // "user" | "assistant" | "system"
    val content: String
)