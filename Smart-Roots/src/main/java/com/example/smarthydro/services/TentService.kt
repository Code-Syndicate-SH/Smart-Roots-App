package com.example.smarthydro.services

import com.example.smarthydro.models.TentModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

private const val BASE_URL = "https://smart-roots-server.onrender.com"

object TentService {
    private val tentClient by lazy {
        Retrofit.Builder().baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TentApi::class.java)
    }

    fun returnTentClient(): TentApi{
        return tentClient
    }

}

interface TentApi {
    @GET("api/tents")
    suspend fun getAllTents(): List<TentModel>

    @GET("api/tents/{macAddress}")
    suspend fun getTentWithMacAddress(@Path("macAddress") macAddress: String)

}