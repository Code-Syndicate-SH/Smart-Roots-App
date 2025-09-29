package com.example.smarthydro.aidailyanomaly.adapter

import com.example.smarthydro.aidailyanomaly.AiDailyConfig
import com.example.smarthydro.aidailyanomaly.domain.model.DailyWindow
import com.example.smarthydro.aidailyanomaly.domain.model.SensorSample
import com.example.smarthydro.aidailyanomaly.ports.SensorLogsPort
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Small self-contained HTTP adapter for this feature only.
 * Later you can replace it with an implementation that uses your existing repository layer.
 */
class MinimalHttpSensorLogsAdapter(
    private val client: OkHttpClient = OkHttpClient(),
    private val moshi: Moshi = Moshi.Builder().build()
) : SensorLogsPort {

    // Match server JSON keys exactly
    private data class ServerReading(
        @Json(name = "macAddress") val mac: String,
        @Json(name = "temperature") val temperature: Double?,
        @Json(name = "humidity") val humidity: Double?,
        @Json(name = "light") val light: Double?,
        @Json(name = "ph") val ph: Double?,
        @Json(name = "ec") val ec: Double?,
        @Json(name = "pumpRuntimeMin") val pumpRuntimeMin: Double?,
        @Json(name = "timestamp") val timestamp: String
    )

    override suspend fun fetchLast24h(window: DailyWindow): List<SensorSample> {
        val url = AiDailyConfig.SERVER_BASE_URL.toHttpUrl().newBuilder()
            .addPathSegment("api") // ← REPLACE if your path differs
            .addPathSegment("logs") // ← REPLACE
            .addQueryParameter("from", window.fromIsoUtc) // ← REPLACE names if needed
            .addQueryParameter("to", window.toIsoUtc)
            .addQueryParameter("mac", window.macAddress)
            .build()

        val req = Request.Builder().url(url).get().build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) error("Logs fetch failed: ${resp.code}")
            val body = resp.body?.string() ?: "[]"
            val type = Types.newParameterizedType(List::class.java, ServerReading::class.java)
            val adapter = moshi.adapter<List<ServerReading>>(type)
            val list = adapter.fromJson(body).orEmpty()
            return list.map {
                SensorSample(
                    macAddress = it.mac,
                    temperature = it.temperature,
                    humidity = it.humidity,
                    light = it.light,
                    ph = it.ph,
                    ec = it.ec,
                    pumpRuntimeMin = it.pumpRuntimeMin,
                    timestampIsoUtc = it.timestamp
                )
            }
        }
    }
}
