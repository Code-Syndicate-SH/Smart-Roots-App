package com.example.smarthydro.services

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.request
import okhttp3.OkHttp
import okhttp3.OkHttpClient

object RemoteClientPing {
    val httpClient= HttpClient(){
        install(HttpTimeout){
            requestTimeoutMillis = 60_000

        }

    }
}