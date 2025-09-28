package com.example.smarthydro.test

import com.example.smarthydro.chat.ChatMessage
import com.example.smarthydro.chat.FredAgent
import com.example.smarthydro.chat.GeminiClient
import com.example.smarthydro.chat.config.AIAgentConfig
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.Assert.assertEquals
import com.example.smarthydro.chat.tools.Tool

class FredAgentTest {

    // Fake LLM that just echoes the last user input
    private val fakeLlm = object : GeminiClient {
        override suspend fun chat(messages: List<ChatMessage>): String {
            return messages.lastOrNull()?.content ?: "No input"
        }
    }

    private val fakeTools: List<Tool> = emptyList()

    @Test
    fun `in-scope question should call LLM and return response`() = runBlocking {
        val agent = FredAgent(fakeLlm, fakeTools)

        val response = agent.reply("How do I adjust pH?", emptyList())

        assertEquals("How do I adjust pH?", response)
    }

    @Test
    fun `out-of-scope question should not call LLM`() = runBlocking {
        val agent = FredAgent(fakeLlm, fakeTools)

        val response = agent.reply("Who won the football match?", emptyList())

        assertEquals(AIAgentConfig.OUT_OF_SCOPE_MESSAGE, response)
    }

    @Test
    fun `clarifying response should trigger AskUser`() = runBlocking {
        val questionLlm = object : GeminiClient {
            override suspend fun chat(messages: List<ChatMessage>): String {
                return "What type of plant are you growing?"
            }
        }
        val agent = FredAgent(questionLlm, fakeTools)

        val response = agent.reply("Tell me how to mix nutrients", emptyList())

        assertEquals("What type of plant are you growing?", response)
    }
}