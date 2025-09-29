package com.example.smarthydro.aidailyanomaly.domain.model

data class DailyReport(
    val macAddress: String,
    val dateUtc: String,      // "YYYY-MM-DD"
    val anomalies: List<Anomaly>,
    val summary: String,
    val status: String        // "HEALTHY" | "ATTENTION"
)
