package com.example.smarthydro.chat.tools

/** Minimal tool contract used by FredAgent. */
interface Tool {
    /** Return true if this tool should handle the user input. */
    fun matches(input: String): Boolean

    /** Execute the tool and return a short, human-readable summary. */
    suspend fun invoke(input: String): ToolResult
}

data class ToolResult(val humanSummary: String)
