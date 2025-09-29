package com.example.smarthydro.aidailyanomaly.ports

import com.example.smarthydro.aidailyanomaly.domain.model.DailyWindow
import com.example.smarthydro.aidailyanomaly.domain.model.SensorSample

interface SensorLogsPort {
    suspend fun fetchLast24h(window: DailyWindow): List<SensorSample>
}
