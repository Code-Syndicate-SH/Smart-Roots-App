package com.example.smarthydro.aidailyanomaly.ports

import com.example.smarthydro.aidailyanomaly.domain.model.DailyReport

interface KoogAgentPort {
    fun summarizeDailyReport(prompt: String): DailyReport
}
