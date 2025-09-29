package com.example.smarthydro.aidailyanomaly.domain.model

data class Anomaly(
    val metric: String,               // "pH" | "EC" | "temperature" | "humidity" | "light" | "pump"
    val description: String,
    val actualValue: Double?,         // null if N/A
    val thresholdLow: Double?,
    val thresholdHigh: Double?,
    val durationMinutes: Int? = null, // for pump downtime etc.
    val severity: Severity
)
