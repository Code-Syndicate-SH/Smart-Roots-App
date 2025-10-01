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

    fun send(text: String) = viewModelScope.launch {
        // Show the user's message immediately
        _ui.update { it.copy(sending = true, messages = it.messages + UiMsg("user", text)) }

        // Get reply from FredAgent
        val reply = agent.reply(text, history)

        // Save to history
        history += ChatMessage("user", text)
        history += ChatMessage("assistant", reply)

        // Show Fred's response
        _ui.update { it.copy(sending = false, messages = it.messages + UiMsg("assistant", reply)) }
    }
}
