package com.example.smarthydro.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UiMsg(
    val role: String,
    val content: String = "",
    val imageBytes: ByteArray? = null // now supports direct Bitmap bytes
)

data class FredUiState(
    val messages: List<UiMsg> = emptyList(),
    val sending: Boolean = false
)

class FredViewModel(
    private val agent: FredAgent
) : ViewModel() {

    private val history = mutableListOf<ChatMessage>()
    private val _ui = MutableStateFlow(FredUiState())
    val ui: StateFlow<FredUiState> = _ui

    // ✉️ Handle normal text
    fun send(text: String) = viewModelScope.launch {
        if (text.isBlank() || _ui.value.sending) return@launch

        _ui.update { it.copy(sending = true, messages = it.messages + UiMsg("user", text)) }

        val reply = runCatching {
            agent.reply(text, history)
        }.getOrElse { th ->
            "Sorry, something went wrong: ${th.localizedMessage ?: th.javaClass.simpleName}"
        }

        history += ChatMessage("user", text)
        history += ChatMessage("assistant", reply)

        _ui.update { it.copy(sending = false, messages = it.messages + UiMsg("assistant", reply)) }
    }

    // 📸 Handle image + question
    fun sendImage(question: String, bytes: ByteArray, mimeType: String = "image/jpeg") =
        viewModelScope.launch {
            if (_ui.value.sending) return@launch

            // show image bubble + question immediately
            _ui.update {
                val imageMsg = UiMsg("user", imageBytes = bytes)
                val textMsg = if (question.isNotBlank()) UiMsg("user", question) else null
                it.copy(
                    sending = true,
                    messages = it.messages + imageMsg + listOfNotNull(textMsg)
                )
            }

            val reply = runCatching {
                agent.replyWithImage(question, bytes, mimeType, history)
            }.getOrElse { th ->
                "Sorry, something went wrong while analyzing the image: ${th.localizedMessage ?: th.javaClass.simpleName}"
            }

            history += ChatMessage("user", "Sent an image with question: $question")
            history += ChatMessage("assistant", reply)

            _ui.update {
                it.copy(
                    sending = false,
                    messages = it.messages + UiMsg("assistant", reply)
                )
            }
        }
}
