package com.example.smarthydro.chat

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class GeminiRestClient(
    private val http: HttpClient,
    private val apiKeyProvider: () -> String
) : GeminiClient {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun chat(model: String, messages: List<ChatMessage>): String {
        val key = apiKeyProvider().orEmpty()
        if (key.isBlank()) return "Gemini API key is missing."

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$key"

        // 🔧 Map roles to what Gemini expects and drop unsupported ones
        val sanitized = messages.mapNotNull { m ->
            when (m.role.lowercase()) {
                "user" -> Content(role = "user", parts = listOf(Part(text = m.content)))
                "assistant", "model" -> Content(role = "model", parts = listOf(Part(text = m.content)))
                "system" -> null // (optional) handle via systemInstruction later
                else -> Content(role = "user", parts = listOf(Part(text = m.content)))
            }
        }

        val req = GeminiRequest(contents = sanitized)

        val resp: HttpResponse = http.post(url) {
            contentType(ContentType.Application.Json)
            setBody(req)
        }
        val body = resp.bodyAsText()

        if (!resp.status.isSuccess()) {
            return "Request failed (${resp.status.value}): ${body.take(200)}"
        }

        val data: GeminiResponse = json.decodeFromString(body)
        val text = data.candidates
            ?.firstOrNull()
            ?.content
            ?.parts
            ?.joinToString("") { part -> part.text.orEmpty() }
            .orEmpty()

        return text.ifBlank { "I couldn’t generate a reply right now." }
    }
}

/* ---------- DTOs ---------- */

@Serializable
data class GeminiRequest(val contents: List<Content>)

@Serializable
data class Content(val role: String? = null, val parts: List<Part>)

@Serializable
data class Part(val text: String? = null)

@Serializable
data class GeminiResponse(val candidates: List<Candidate>?)

@Serializable
data class Candidate(val content: ContentParts)

@Serializable
data class ContentParts(val parts: List<Part>)
