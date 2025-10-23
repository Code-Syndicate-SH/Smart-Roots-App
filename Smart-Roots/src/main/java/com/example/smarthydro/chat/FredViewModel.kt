package com.example.smarthydro.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ----------------- UI Models -----------------
data class UiMsg(val role: String, val content: String)
data class FredUiState(val messages: List<UiMsg> = emptyList(), val sending: Boolean = false)

// 🔑 Mapper: converts UiMsg -> ChatUiMessage (for rendering in FredChatScreen)
fun UiMsg.toUiMessage(): ChatUiMessage =
    if (role.equals("user", ignoreCase = true)) {
        ChatUiMessage(text = content, from = ChatUiMessage.Sender.USER)
    } else {
        ChatUiMessage(text = content, from = ChatUiMessage.Sender.FRED)
    }

// ----------------- ViewModel -----------------
class FredViewModel(
    private val agent: FredAgent
) : ViewModel() {

    private val history = mutableListOf<ChatMessage>()
    private val _ui = MutableStateFlow(FredUiState())
    val ui: StateFlow<FredUiState> = _ui

    // ✉️ Text messages
    fun send(text: String) = viewModelScope.launch {
        if (text.isBlank() || _ui.value.sending) return@launch

        // Show user’s message immediately
        _ui.update { it.copy(sending = true, messages = it.messages + UiMsg("user", text)) }

        // Ask Fred for a reply
        val reply = runCatching {
            agent.reply(text, history)
        }.getOrElse { th ->
            "Sorry, something went wrong: ${th.localizedMessage ?: th.javaClass.simpleName}"
        }

        // Update conversation history
        history += ChatMessage("user", text)
        history += ChatMessage("assistant", reply)

        // Show Fred's reply
        _ui.update { it.copy(sending = false, messages = it.messages + UiMsg("assistant", reply)) }
    }

    // 📸 Image + Question messages
    fun sendImage(question: String, imageBytes: ByteArray, mimeType: String) =
        viewModelScope.launch {
            if (_ui.value.sending) return@launch

            // 1️⃣ Show image immediately in chat
            _ui.update {
                it.copy(
                    sending = true,
                    messages = it.messages + UiMsg("user", "image://local_preview") +
                            UiMsg("user", question.ifBlank { "Analyze this plant [📷]" })
                )
            }

            // 2️⃣ Get reply from Fred (analyze image)
            val reply = runCatching {
                agent.replyWithImage(question, imageBytes, mimeType, history)
            }.getOrElse { th ->
                "Sorry, something went wrong: ${th.localizedMessage ?: th.javaClass.simpleName}"
            }

            // 3️⃣ Update stored chat history
            history += ChatMessage("user", "Sent an image with question: $question")
            history += ChatMessage("assistant", reply)

            // 4️⃣ Add Fred’s reply to chat
            _ui.update {
                it.copy(
                    sending = false,
                    messages = it.messages + UiMsg("assistant", reply)
                )
            }
        }
}
