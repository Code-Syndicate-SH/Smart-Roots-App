package com.example.smarthydro.aidailyanomaly.domain.usecase

object DefaultThresholds {
    // Replace with your agreed targets
    const val TEMP_MIN = 18.0
    const val TEMP_MAX = 28.0
    const val HUMID_MIN = 45.0
    const val HUMID_MAX = 70.0
    const val LIGHT_MIN = 500.0
    const val LIGHT_MAX = 30000.0
    const val PH_MIN = 5.5
    const val PH_MAX = 6.5
    const val EC_MIN = 1.2
    const val EC_MAX = 2.2
    const val PUMP_DOWNTIME_MAX_HOURS = 10
}
