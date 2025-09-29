package com.example.smarthydro.aidailyanomaly.worker

import android.content.Context
import androidx.work.*
import com.example.smarthydro.aidailyanomaly.AiDailyConfig
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

object Scheduler {
    fun scheduleDaily(context: Context) {
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        val target = now.withHour(23).withMinute(55).withSecond(0).withNano(0) // change time if you want
        val first = if (now.isAfter(target)) target.plusDays(1) else target
        val delayMin = Duration.between(now, first).toMinutes()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        // schedule a unique work for each device (tent)
        AiDailyConfig.DEVICES.forEach { device ->
            val data = Data.Builder()
                .putString("mac", device.mac)
                .putString("label", device.label)
                .build()

            val req = PeriodicWorkRequestBuilder<AnomalyCheckWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(delayMin, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setInputData(data) // pass device info to the worker
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                /* unique name per device */ "daily-anomaly-check-${device.mac}",
                ExistingPeriodicWorkPolicy.UPDATE,
                req
            )
        }
    }
}
