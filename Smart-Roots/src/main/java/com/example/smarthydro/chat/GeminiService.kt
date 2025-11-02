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
        if (key.isBlank()) return "I can’t reach the model right now. Please try again."

        val contents = buildContentsWithSystem(messages)
        val req = GeminiRequest(
            contents = contents,
            generationConfig = GenerationConfig(
                temperature = AIAgentConfig.TEMPERATURE,
                maxOutputTokens = AIAgentConfig.MAX_OUTPUT_TOKENS,
                responseMimeType = "text/plain"
            )
        )
        return multiTryCall(req, key)
    }

    // ---------------------- IMAGE + QUESTION ----------------------
    override suspend fun chatImage(
        question: String,
        imageBytes: ByteArray,
        mimeType: String,
        history: List<ChatMessage>
    ): String {
        val key = apiKeyProvider().orEmpty()
        if (key.isBlank()) return "I can’t reach the model right now. Please try again."

        val base = buildContentsWithSystem(history)
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
        return multiTryCall(req, key)
    }

    // ---------------------- Helpers ----------------------
    /** Inject system text as first user message and map roles to user/model. */
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
                "assistant", "model" -> Content(role = "model", parts = listOf(Part(m.content)))
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

    /** Prefer v1beta for 2.5; then v1. Rotate models on 404 only. */
    private suspend fun multiTryCall(req: GeminiRequest, key: String): String {
        val versions = listOf("v1beta", "v1")
        val models = AIAgentConfig.MODEL_CANDIDATES
        var lastFriendly = "I couldn’t generate a reply right now."

        for (ver in versions) {
            for (model in models) {
                val (ok, out, code) = call(ver, model, key, req)
                if (ok) return out
                lastFriendly = out
                if (code != 404) return lastFriendly
            }
        }
        return lastFriendly
    }

    /**
     * Returns Triple<ok, message, httpStatus?>.
     *  - Success: ok=true,  message=text
     *  - Error:   ok=false, message=friendly text, httpStatus set
     */
    private suspend fun call(
        apiVersion: String,
        model: String,
        key: String,
        req: GeminiRequest
    ): Triple<Boolean, String, Int?> {
        val safeModel = model.removePrefix("models/").substringBefore(":")
        val url = "https://generativelanguage.googleapis.com/$apiVersion/models/$safeModel:generateContent?key=$key"

        val resp: HttpResponse = http.post(url) {
            contentType(ContentType.Application.Json)
            setBody(req)
        }
        val body = resp.bodyAsText()

        if (!resp.status.isSuccess()) {
            val code = resp.status.value
            val serverMsg = extractGoogleErrorMessage(body)
            val friendly = mapFriendlyError(code, serverMsg)
            return Triple(false, friendly, code)
        }

        val data = runCatching { json.decodeFromString<GeminiResponse>(body) }.getOrNull()
            ?: return Triple(true, "I couldn’t read the model response.", null)

        val cand = data.candidates?.firstOrNull()

        // 1) 2.x convenience string
        val t1 = cand?.text?.takeIf { !it.isNullOrBlank() }

        // 2) Classic content.parts[].text
        val t2 = cand?.content?.parts
            ?.mapNotNull { it.text }
            ?.joinToString("")?.takeIf { it.isNotBlank() }

        // 3) Safety / early stop
        val t3 = when {
            cand?.finishReason?.contains("SAFETY", true) == true ->
                "Response was blocked by safety."
            data.promptFeedback?.blockReason?.isNullOrBlank() == false ->
                "Response was blocked by safety: ${data.promptFeedback.blockReason}"
            cand?.finishReason?.contains("MAX_TOKENS", true) == true ->
                "Output was cut early (max tokens). Please ask for a shorter answer."
            else -> null
        }

        val text = t1 ?: t2 ?: t3 ?: "I couldn’t generate a reply right now."
        return Triple(true, text, null)
    }

    /* ---------- Error handling ---------- */

    @Serializable private data class GoogleErrorEnvelope(val error: GoogleError? = null)
    @Serializable private data class GoogleError(
        val code: Int? = null,
        val message: String? = null,
        val status: String? = null
    )

    private fun extractGoogleErrorMessage(body: String): String? =
        runCatching { json.decodeFromString<GoogleErrorEnvelope>(body).error?.message }.getOrNull()
            ?: body.takeIf { it.isNotBlank() }?.take(180)

    private fun mapFriendlyError(code: Int, serverMsg: String?): String = when (code) {
        400 -> "I couldn’t understand that request. Please rephrase and try again."
        401 -> "I can’t reach the model right now. Please try again."
        403 -> "This model isn’t available for the current key. Please try again later."
        404 -> "That model isn’t available. Trying a different one…"
        408, 504 -> "The request timed out. Please try again."
        429 -> "I’m getting a lot of requests at once. Please try again shortly."
        500, 502, 503 -> "The model is busy right now. Please try again in a moment."
        else -> sanitize(serverMsg ?: "")
    }

    /** Trim server text so we never dump raw JSON to users. */
    private fun sanitize(msg: String): String =
        msg.replace(Regex("\\s+"), " ").trim().let { s ->
            if (s.isBlank()) "I couldn’t respond right now. Please try again." else s.take(160)
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
