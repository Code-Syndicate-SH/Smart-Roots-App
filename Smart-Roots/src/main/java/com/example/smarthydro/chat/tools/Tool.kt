package com.example.smarthydro.chat.tools

interface Tool {
    fun matches(userText: String): Boolean
    suspend fun invoke(userText: String): ToolResult
}

data class ToolResult(val humanSummary: String)
