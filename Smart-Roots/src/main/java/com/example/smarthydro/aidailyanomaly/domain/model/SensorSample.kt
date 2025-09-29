package com.example.smarthydro.aidailyanomaly.domain.model

data class SensorSample(
    val macAddress: String,
    val temperature: Double?,    // °C
    val humidity: Double?,       // %
    val light: Double?,          // lux (or your unit)
    val ph: Double?,
    val ec: Double?,             // mS/cm
    val pumpRuntimeMin: Double?, // minutes in the sample interval
    val timestampIsoUtc: String  // ISO-8601 e.g., 2025-09-26T12:00:00Z
)
