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
    var input by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Fred • SmartHydro", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        LazyColumn(Modifier.weight(1f)) {
            items(state.messages) { m ->
                Text("${m.role}: ${m.text}")
                Spacer(Modifier.height(8.dp))
            }
        }
        Row {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message…") }
            )
            Spacer(Modifier.width(8.dp))
            Button(
                enabled = !state.sending,
                onClick = { vm.send(input); input = "" }
            ) { Text(if (state.sending) "…" else "Send") }
        }
    }
}
