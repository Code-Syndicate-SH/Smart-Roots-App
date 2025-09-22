package com.example.smarthydro.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UiMsg(val role: String, val content: String)
data class FredUiState(val messages: List<UiMsg> = emptyList(), val sending: Boolean = false)

class FredViewModel(
    private val agent: FredAgent
) : ViewModel() {

    private val history = mutableListOf<ChatMessage>()
    private val _ui = MutableStateFlow(FredUiState())
    val ui: StateFlow<FredUiState> = _ui

    fun send(text: String) = viewModelScope.launch {
        _ui.update { it.copy(sending = true, messages = it.messages + UiMsg("user", text)) }
        val reply = agent.reply(text, history)
        history += ChatMessage("user", text)
        history += ChatMessage("assistant", reply)
        _ui.update { it.copy(sending = false, messages = it.messages + UiMsg("assistant", reply)) }
    }
}
