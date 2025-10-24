package com.example.smarthydro.domain

import android.util.Log
import com.example.smarthydro.models.RemoteSensorModel
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException

class SensorStreamClient(
    private val serverUrl: String,
    private val onMessage: (RemoteSensorModel) -> Unit
) {
    private val client = OkHttpClient()
    private val gson = Gson()
    private var call: Call? = null

    fun start() {
        val request = Request.Builder()
            .url(serverUrl)
            .header("Accept", "text/event-stream")
            .build()

        call = client.newCall(request)
        call?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("SSE", "Connection failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.use { body ->
                    val source = body.source()
                    while (true) {
                        val line = source.readUtf8Line() ?: break
                        if (line.startsWith("data:")) {
                            val json = line.removePrefix("data:").trim()
                            try {
                                val reading = gson.fromJson(json, RemoteSensorModel::class.java)
                                onMessage(reading)
                            } catch (ex: Exception) {
                                Log.e("SSE", "JSON parse error: $ex")
                            }
                        }
                    }
                }
            }
        })
    }

    fun stop() {
        call?.cancel()
    }
}