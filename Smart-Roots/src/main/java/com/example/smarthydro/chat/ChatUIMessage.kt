package com.example.smarthydro.chat

/** UI-only model for rendering chat items as cards. */
data class ChatUiMessage(
    val id: Long = System.currentTimeMillis(),
    val text: String,
    val from: Sender
) {
    enum class Sender { USER, FRED }
}

/** Helper to map your existing ChatMessage -> ChatUiMessage */
fun ChatMessage.toUi(): ChatUiMessage =
    if (role.equals("user", ignoreCase = true))
        ChatUiMessage(text = content, from = ChatUiMessage.Sender.USER)
    else
        ChatUiMessage(text = content, from = ChatUiMessage.Sender.FRED)
