package com.example.smarthydro.aidailyanomaly.domain.model

data class DailyWindow(
    val fromIsoUtc: String,
    val toIsoUtc: String,
    val macAddress: String
)
