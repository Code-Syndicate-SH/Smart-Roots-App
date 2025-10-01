package com.example.smarthydro.chat

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@Composable
fun FredScreen(
    viewModel: FredViewModel = koinViewModel()
) {
    val state = viewModel.ui.collectAsStateWithLifecycle().value

    FredChatScreen(
        messages = state.messages,            // <-- pass UiMsg directly
        isThinking = state.sending,
        onSend = { text -> viewModel.send(text) }
    )
}
