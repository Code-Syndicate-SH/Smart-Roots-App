package com.example.smarthydro.chat

import com.example.smarthydro.chat.config.AIAgentConfig
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

class GeminiService(
    private val http: HttpClient,
    private val apiKeyProvider: () -> String
) : GeminiClient {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    override suspend fun chat(messages: List<ChatMessage>): String {
        val key = apiKeyProvider().orEmpty()
        if (key.isBlank()) return "Gemini API key is missing."

        // Build contents (map roles) and inject system prompt as first user message
        val systemFromHistory = messages
            .filter { it.role.equals("system", true) }
            .joinToString("\n") { it.content }
            .ifBlank { null }

        val combinedSystem = listOfNotNull(
            AIAgentConfig.SYSTEM_PROMPT,
            systemFromHistory
        ).joinToString("\n\n").ifBlank { null }

        val mapped = messages.mapNotNull { m ->
            when (m.role.lowercase()) {
                "user" -> Content(role = "user", parts = listOf(Part(m.content)))
                "assistant", "model" -> Content(role = "model", parts = listOf(Part(m.content)))
                "system" -> null // moved into combinedSystem
                else -> Content(role = "user", parts = listOf(Part(m.content)))
            }
        }

        val contents = buildList {
            if (!combinedSystem.isNullOrBlank()) {
                // v1/v1beta friendly “system” injection
                add(Content(role = "user", parts = listOf(Part(combinedSystem))))
            }
            addAll(mapped)
        }

        val req = GeminiRequest(
            contents = contents,
            generationConfig = GenerationConfig(
                temperature = AIAgentConfig.TEMPERATURE,
                maxOutputTokens = AIAgentConfig.MAX_OUTPUT_TOKENS,
                responseMimeType = "text/plain" // <- ask for plain text
            )
        )

        // Prefer v1beta for 2.5; then try v1. Try your configured models in order.
        val versions = listOf("v1beta", "v1")
        val models = AIAgentConfig.MODEL_CANDIDATES
        var lastErr = "Unknown error"

        for (ver in versions) {
            for (model in models) {
                val (ok, out) = call(ver, model, key, req)
                if (ok) return out
                lastErr = out
                if (!out.startsWith("404:")) break // only rotate models on 404
            }
            if (!lastErr.startsWith("404:")) break // only rotate versions on 404
        }
        return lastErr
    }

    private suspend fun call(
        apiVersion: String,
        model: String,
        key: String,
        req: GeminiRequest
    ): Pair<Boolean, String> {
        val safeModel = model.removePrefix("models/").substringBefore(":")
        val url = "https://generativelanguage.googleapis.com/$apiVersion/models/$safeModel:generateContent?key=$key"

        val resp: HttpResponse = http.post(url) {
            contentType(ContentType.Application.Json)
            setBody(req)
        }
        val body = resp.bodyAsText()

        if (!resp.status.isSuccess()) {
            return false to "${resp.status.value}: ${body.take(240)}"
        }

        // Parse leniently and pull text from several possible locations
        val data = runCatching { json.decodeFromString<GeminiResponse>(body) }.getOrNull()
            ?: return true to "I couldn’t read the model response."

        val candidate = data.candidates?.firstOrNull()

        // 1) Gemini 2.x sometimes returns a direct string here
        val text1 = candidate?.text?.takeIf { !it.isNullOrBlank() }

        // 2) Classic path: content.parts[].text
        val text2 = candidate?.content?.parts
            ?.mapNotNull { it.text }
            ?.joinToString("")?.takeIf { it.isNotBlank() }

        // 3) Safety/early-stop signals
        val text3 = when {
            candidate?.finishReason?.contains("SAFETY", true) == true ->
                "Response blocked by safety."
            data.promptFeedback?.blockReason?.isNullOrBlank() == false ->
                "Response blocked by safety: ${data.promptFeedback.blockReason}"
            candidate?.finishReason?.contains("MAX_TOKENS", true) == true ->
                "Output cut early (max tokens). Try again or ask for a shorter answer."
            else -> null
        }

        val text = text1 ?: text2 ?: text3
        return true to (text?.trim().takeUnless { it.isNullOrEmpty() }
            ?: "I couldn’t generate a reply right now.")
    }
}

/* ---------- API DTOs (lenient / optional fields) ---------- */

@Serializable
data class GeminiRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null
)

@Serializable
data class Content(val role: String? = null, val parts: List<Part>? = null)

@Serializable
data class Part(val text: String? = null)

@Serializable
data class GenerationConfig(
    val temperature: Double? = null,
    val maxOutputTokens: Int? = null,
    val topP: Double? = null,
    val topK: Int? = null,
    val responseMimeType: String? = null // <- new
)

@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>? = null,
    val promptFeedback: PromptFeedback? = null
)

@Serializable
data class Candidate(
    val content: ContentParts? = null,
    val text: String? = null,            // 2.x convenience field (when present)
    val finishReason: String? = null
)

@Serializable
data class ContentParts(
    val role: String? = null,
    val parts: List<Part>? = null        // optional; 2.x may omit
)

@Serializable
data class PromptFeedback(val blockReason: String? = null)
