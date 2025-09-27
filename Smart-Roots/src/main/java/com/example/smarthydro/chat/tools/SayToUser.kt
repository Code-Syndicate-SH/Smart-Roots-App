package com.example.smarthydro.chat.tools

/**
 * Tool for sending messages to the user.
 * Currently just logs to console. Later, connect to your UI layer.
 */
object SayToUser {
    fun send(message: String) {
        println("🤖 Agent says: $message")
        // TODO: Hook this into chat UI instead of println
    }
}
