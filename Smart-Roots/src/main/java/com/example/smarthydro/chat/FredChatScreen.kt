package com.example.smarthydro.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class) // ✅ suppress experimental warning
@Composable
fun FredChatScreen(
    messages: List<UiMsg>,
    isThinking: Boolean,
    onSend: (String) -> Unit
) {
    var userInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101010)) // Dark background like your app
            .padding(8.dp)
    ) {
        // Message list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Bottom
        ) {
            items(messages) { message ->
                val isUser = message.role == "user"
                val bubbleColor = if (isUser) Color(0xFF4CAF50) else Color(0xFF1E1E1E) // ✅ Green for user
                val textColor = Color.White

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = bubbleColor,
                        tonalElevation = 4.dp,
                        shadowElevation = 4.dp,
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Text(
                            text = message.content,
                            color = textColor, // ✅ fixed param
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        if (isThinking) {
            Text(
                text = "Fred is thinking...",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }

        // Input row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = userInput,
                onValueChange = { userInput = it },
                placeholder = { Text("Type your message...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color(0xFF1E1E1E),
                    focusedBorderColor = Color(0xFF4CAF50),
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            IconButton(
                onClick = {
                    if (userInput.isNotBlank()) {
                        onSend(userInput)
                        userInput = ""
                    }
                },
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(48.dp)
                    .background(Color(0xFF4CAF50), shape = RoundedCornerShape(50))
            ) {
                Icon(
                    imageVector = Icons.Filled.Send, // ✅ fixed import
                    contentDescription = "Send",
                    tint = Color.White
                )
            }
        }
    }
}
