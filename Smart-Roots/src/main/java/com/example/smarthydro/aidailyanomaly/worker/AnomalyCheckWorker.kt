package com.example.smarthydro.aidailyanomaly.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.smarthydro.aidailyanomaly.AiDailyConfig
import com.example.smarthydro.aidailyanomaly.adapter.KoogHttpClient
import com.example.smarthydro.aidailyanomaly.adapter.MinimalHttpSensorLogsAdapter
import com.example.smarthydro.aidailyanomaly.domain.model.DailyReport
import com.example.smarthydro.aidailyanomaly.domain.model.DailyWindow
import com.example.smarthydro.aidailyanomaly.domain.usecase.BuildKoogSchemaUseCase
import com.example.smarthydro.aidailyanomaly.domain.usecase.EvaluateAnomaliesUseCase
import com.example.smarthydro.aidailyanomaly.ui.notifications.Notifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.ZoneId
import java.time.ZonedDateTime

class AnomalyCheckWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork() = withContext(Dispatchers.IO) {
        try {
            //  prefer per-task input; fall back to single-device config
            val mac   = inputData.getString("mac")   ?: AiDailyConfig.DEVICE_MAC
            val label = inputData.getString("label") ?: "Device"

            val nowUtc = ZonedDateTime.now(ZoneId.of("UTC"))
            val from = nowUtc.minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0)
            val to   = nowUtc.withHour(0).withMinute(0).withSecond(0).withNano(0)

            val window = DailyWindow(
                fromIsoUtc = from.toLocalDateTime().toString() + "Z",
                toIsoUtc   = to.toLocalDateTime().toString() + "Z",
                macAddress = mac
            )

            val logsPort = MinimalHttpSensorLogsAdapter()
            val samples = logsPort.fetchLast24h(window)

            val anomalies = EvaluateAnomaliesUseCase().evaluate(samples)
            val dateUtc = from.toLocalDate().toString()

            val report: DailyReport = if (anomalies.isEmpty()) {
                DailyReport(
                    macAddress = mac,
                    dateUtc = dateUtc,
                    anomalies = emptyList(),
                    summary = "System healthy — no anomalies detected in the last 24 hours.",
                    status = "HEALTHY"
                )
            } else {
                val prompt = BuildKoogSchemaUseCase().makePrompt(mac, dateUtc, anomalies)
                KoogHttpClient().summarizeDailyReport(prompt)
            }

            // include label + mac so notifications are distinct per tent
            Notifier.showDailyResult(applicationContext, report, label, mac)
            Result.success()
        } catch (t: Throwable) {
            t.printStackTrace()
            Result.retry()
        }
    }
}
