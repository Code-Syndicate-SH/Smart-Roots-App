package com.example.smarthydro.chat

import android.util.Base64
import com.example.smarthydro.chat.config.AIAgentConfig
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
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

    // ---------------------- TEXT CHAT ----------------------
    override suspend fun chat(messages: List<ChatMessage>): String {
        val key = apiKeyProvider().orEmpty()
        if (key.isBlank()) return "Gemini API key is missing."

        val contents = buildContentsWithSystem(messages)

        val req = GeminiRequest(
            contents = contents,
            generationConfig = GenerationConfig(
                temperature = AIAgentConfig.TEMPERATURE,
                maxOutputTokens = AIAgentConfig.MAX_OUTPUT_TOKENS,
                responseMimeType = "text/plain"
            )
        )

        return multiTryCall(req)
    }

    // ---------------------- IMAGE + QUESTION ----------------------
    override suspend fun chatImage(
        question: String,
        imageBytes: ByteArray,
        mimeType: String,
        history: List<ChatMessage>
    ): String {
        val key = apiKeyProvider().orEmpty()
        if (key.isBlank()) return "Gemini API key is missing."

        // Build the same system+history as text chat (but exclude any 'system' from history)
        val base = buildContentsWithSystem(history)

        // Add the user question + image as one content with two parts
        val b64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
        val imageUser = Content(
            role = "user",
            parts = listOf(
                Part(text = question),
                Part(inlineData = InlineData(mimeType = mimeType, data = b64))
            )
        )

        val req = GeminiRequest(
            contents = base + imageUser,
            generationConfig = GenerationConfig(
                temperature = AIAgentConfig.TEMPERATURE,
                maxOutputTokens = AIAgentConfig.MAX_OUTPUT_TOKENS,
                responseMimeType = "text/plain"
            )
        )

        return multiTryCall(req)
    }

    // ---------------------- Helpers ----------------------
    /** Injects system text as the first user message and maps roles to user/model. */
    private fun buildContentsWithSystem(messages: List<ChatMessage>): List<Content> {
        val systemFromHistory = messages
            .filter { it.role.equals("system", true) }
            .joinToString("\n") { it.content }
            .ifBlank { null }

        val combinedSystem = listOfNotNull(AIAgentConfig.SYSTEM_PROMPT, systemFromHistory)
            .joinToString("\n\n").ifBlank { null }

        val mappedHistory = messages.mapNotNull { m ->
            when (m.role.lowercase()) {
                "user" -> Content(role = "user", parts = listOf(Part(text = m.content)))
                "assistant", "model" -> Content(role = "model", parts = listOf(Part(text = m.content)))
                "system" -> null // moved into combinedSystem
                else -> Content(role = "user", parts = listOf(Part(text = m.content)))
            }
        }

        return buildList {
            if (!combinedSystem.isNullOrBlank()) {
                add(Content(role = "user", parts = listOf(Part(text = combinedSystem))))
            }
            addAll(mappedHistory)
        }
    }

    /** Try v1beta first (best for 2.5), then v1; try all configured models in order. */
    private suspend fun multiTryCall(req: GeminiRequest): String {
        val key = apiKeyProvider().orEmpty()
        val versions = listOf("v1beta", "v1")
        val models = AIAgentConfig.MODEL_CANDIDATES
        var lastErr = "Unknown error"

        for (ver in versions) {
            for (model in models) {
                val (ok, out) = call(ver, model, key, req)
                if (ok) return out
                lastErr = out
                if (!out.startsWith("404:")) break // rotate model only on 404
            }
            if (!lastErr.startsWith("404:")) break // rotate version only on 404
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

        val data = runCatching { json.decodeFromString<GeminiResponse>(body) }.getOrNull()
            ?: return true to "I couldn’t read the model response."

        val cand = data.candidates?.firstOrNull()

        // 1) Gemini 2.x convenience string
        val t1 = cand?.text?.takeIf { !it.isNullOrBlank() }

        // 2) Classic content.parts[].text
        val t2 = cand?.content?.parts
            ?.mapNotNull { it.text }
            ?.joinToString("")?.takeIf { it.isNotBlank() }

        // 3) Safety / early-stop info
        val t3 = when {
            cand?.finishReason?.contains("SAFETY", true) == true ->
                "Response blocked by safety."
            data.promptFeedback?.blockReason?.isNullOrBlank() == false ->
                "Response blocked by safety: ${data.promptFeedback.blockReason}"
            cand?.finishReason?.contains("MAX_TOKENS", true) == true ->
                "Output cut early (max tokens). Try again or ask for a shorter answer."
            else -> null
        }

        val text = t1 ?: t2 ?: t3
        return true to (text?.trim().takeUnless { it.isNullOrEmpty() }
            ?: "I couldn’t generate a reply right now.")
    }
}

/* ---------- API DTOs (lenient / multimodal) ---------- */

@Serializable
data class GeminiRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null
)

@Serializable
data class Content(
    val role: String? = null,
    val parts: List<Part>? = null
)

@Serializable
data class Part(
    val text: String? = null,
    @SerialName("inlineData") val inlineData: InlineData? = null // for image inputs
)

@Serializable
data class InlineData(
    @SerialName("mimeType") val mimeType: String,
    val data: String // base64 image bytes
)

@Serializable
data class GenerationConfig(
    val temperature: Double? = null,
    val maxOutputTokens: Int? = null,
    val topP: Double? = null,
    val topK: Int? = null,
    val responseMimeType: String? = null
)

@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>? = null,
    val promptFeedback: PromptFeedback? = null
)

@Serializable
data class Candidate(
    val content: ContentParts? = null,
    val text: String? = null,
    val finishReason: String? = null
)

@Serializable
data class ContentParts(
    val role: String? = null,
    val parts: List<Part>? = null
)

@Serializable
data class PromptFeedback(
    val blockReason: String? = null
)
