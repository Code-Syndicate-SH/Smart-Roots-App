package com.example.smarthydro.aidailyanomaly.domain.usecase

import com.example.smarthydro.aidailyanomaly.domain.model.*
import kotlin.math.round

class EvaluateAnomaliesUseCase {

    fun evaluate(samples: List<SensorSample>): List<Anomaly> {
        if (samples.isEmpty()) return emptyList()
        val out = mutableListOf<Anomaly>()

        fun List<Double?>.avg() = filterNotNull().average().takeIf { !it.isNaN() }
        fun dbl(x: Double?) = x?.let { round(it * 10) / 10.0 }

        val tAvg = samples.map { it.temperature }.avg()
        if (tAvg != null && (tAvg < DefaultThresholds.TEMP_MIN || tAvg > DefaultThresholds.TEMP_MAX)) {
            out += Anomaly(
                "temperature",
                "Avg temperature ${dbl(tAvg)}°C outside ${DefaultThresholds.TEMP_MIN}-${DefaultThresholds.TEMP_MAX}°C",
                tAvg, DefaultThresholds.TEMP_MIN, DefaultThresholds.TEMP_MAX, null,
                if (tAvg < DefaultThresholds.TEMP_MIN - 2 || tAvg > DefaultThresholds.TEMP_MAX + 2) Severity.SEVERE else Severity.MODERATE
            )
        }

        val hAvg = samples.map { it.humidity }.avg()
        if (hAvg != null && (hAvg < DefaultThresholds.HUMID_MIN || hAvg > DefaultThresholds.HUMID_MAX)) {
            out += Anomaly(
                "humidity",
                "Avg humidity ${dbl(hAvg)}% outside ${DefaultThresholds.HUMID_MIN}-${DefaultThresholds.HUMID_MAX}%",
                hAvg, DefaultThresholds.HUMID_MIN, DefaultThresholds.HUMID_MAX, null,
                Severity.MODERATE
            )
        }

        val lAvg = samples.map { it.light }.avg()
        if (lAvg != null && (lAvg < DefaultThresholds.LIGHT_MIN || lAvg > DefaultThresholds.LIGHT_MAX)) {
            out += Anomaly(
                "light",
                "Avg light ${dbl(lAvg)} outside ${DefaultThresholds.LIGHT_MIN}-${DefaultThresholds.LIGHT_MAX}",
                lAvg, DefaultThresholds.LIGHT_MIN, DefaultThresholds.LIGHT_MAX, null,
                Severity.MILD
            )
        }

        val pAvg = samples.map { it.ph }.avg()
        if (pAvg != null && (pAvg < DefaultThresholds.PH_MIN || pAvg > DefaultThresholds.PH_MAX)) {
            out += Anomaly(
                "pH",
                "Avg pH ${dbl(pAvg)} outside ${DefaultThresholds.PH_MIN}-${DefaultThresholds.PH_MAX}",
                pAvg, DefaultThresholds.PH_MIN, DefaultThresholds.PH_MAX, null,
                Severity.MODERATE
            )
        }

        val eAvg = samples.map { it.ec }.avg()
        if (eAvg != null && (eAvg < DefaultThresholds.EC_MIN || eAvg > DefaultThresholds.EC_MAX)) {
            out += Anomaly(
                "EC",
                "Avg EC ${dbl(eAvg)} mS/cm outside ${DefaultThresholds.EC_MIN}-${DefaultThresholds.EC_MAX}",
                eAvg, DefaultThresholds.EC_MIN, DefaultThresholds.EC_MAX, null,
                Severity.MODERATE
            )
        }

        val totalPumpMin = samples.sumOf { it.pumpRuntimeMin ?: 0.0 }
        val downtimeHours = 24.0 - (totalPumpMin / 60.0)
        if (downtimeHours > DefaultThresholds.PUMP_DOWNTIME_MAX_HOURS) {
            out += Anomaly(
                "pump",
                "Pump downtime ~${downtimeHours.toInt()}h exceeds ${DefaultThresholds.PUMP_DOWNTIME_MAX_HOURS}h",
                null, null, null,
                (downtimeHours * 60).toInt(),
                Severity.SEVERE
            )
        }

        return out
    }
}
