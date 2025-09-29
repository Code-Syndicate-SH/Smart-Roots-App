package com.example.smarthydro.aidailyanomaly.domain.usecase

import com.example.smarthydro.aidailyanomaly.domain.model.Anomaly

class BuildKoogSchemaUseCase {
    fun makePrompt(mac: String, dateUtc: String, anomalies: List<Anomaly>): String {
        val bullets = if (anomalies.isEmpty())
            "No anomalies detected. System appears healthy."
        else anomalies.joinToString("\n") { "- ${it.metric}: ${it.description} (severity=${it.severity})" }

        return """
        Produce a DailyReport JSON for device $mac on $dateUtc.
        Status rule: HEALTHY if no anomalies, else ATTENTION.
        Anomalies:
        $bullets
        """.trimIndent()
    }
}
