package com.example.smarthydro.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@Composable
fun FredScreen(vm: FredViewModel = koinViewModel()) {
    val state by vm.ui.collectAsState()
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        LazyColumn(Modifier.weight(1f)) {
            items(state.messages) { m ->
                Text("${m.role}: ${m.content}")
                Spacer(Modifier.height(8.dp))
            }
        }
        var input by remember { mutableStateOf("") }
        Row(Modifier.fillMaxWidth()) {
            OutlinedTextField(input, { input = it }, modifier = Modifier.weight(1f), placeholder = { Text("Ask Fred…") })
            Spacer(Modifier.width(8.dp))
            Button(enabled = input.isNotBlank() && !state.sending, onClick = { vm.send(input); input = "" }) {
                Text("Send")
            }
        }
    }
}
