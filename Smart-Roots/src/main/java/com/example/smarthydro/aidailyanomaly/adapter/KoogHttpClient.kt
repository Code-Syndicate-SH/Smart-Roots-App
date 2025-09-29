package com.example.smarthydro.aidailyanomaly.adapter

import com.example.smarthydro.aidailyanomaly.AiDailyConfig
import com.example.smarthydro.aidailyanomaly.domain.model.DailyReport
import com.example.smarthydro.aidailyanomaly.ports.KoogAgentPort
import com.squareup.moshi.Moshi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object KoogSchemas {
    val DAILY_REPORT_SCHEMA = """
    Return ONLY valid JSON:
    {
      "macAddress": "string",
      "dateUtc": "YYYY-MM-DD",
      "anomalies": [
        {
          "metric": "pH|EC|temperature|humidity|light|pump",
          "description": "string",
          "actualValue": number | null,
          "thresholdLow": number | null,
          "thresholdHigh": number | null,
          "durationMinutes": number | null,
          "severity": "MILD|MODERATE|SEVERE"
        }
      ],
      "summary": "string",
      "status": "HEALTHY|ATTENTION"
    }
    """.trimIndent()
}

class KoogHttpClient(
    private val client: OkHttpClient = OkHttpClient(),
    private val moshi: Moshi = Moshi.Builder().build()
) : KoogAgentPort {

    override fun summarizeDailyReport(prompt: String): DailyReport {
        val endpoint = "${AiDailyConfig.KOOG_BASE_URL.trimEnd('/')}/structured" // adjust to Koog docs
        val media = "application/json".toMediaType()
        val payload = """
            {
              "schema": ${KoogSchemas.DAILY_REPORT_SCHEMA.quoteAsJson()},
              "prompt": ${prompt.quoteAsJson()}
            }
        """.trimIndent()

        val req = Request.Builder()
            .url(endpoint)
            .addHeader("Authorization", "Bearer ${AiDailyConfig.KOOG_API_KEY}") // ← REPLACE header name if needed
            .post(payload.toRequestBody(media))
            .build()

        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) error("Koog error: ${resp.code}")
            val json = resp.body?.string() ?: error("Koog empty body")
            val adapter = moshi.adapter(DailyReport::class.java)
            return adapter.fromJson(json) ?: error("Koog returned invalid JSON")
        }
    }
}

private fun String.quoteAsJson(): String = buildString {
    append('"')
    for (c in this@quoteAsJson) when (c) {
        '\\' -> append("\\\\")
        '"'  -> append("\\\"")
        '\n' -> append("\\n")
        '\r' -> append("\\r")
        '\t' -> append("\\t")
        else -> append(c)
    }
    append('"')
}
