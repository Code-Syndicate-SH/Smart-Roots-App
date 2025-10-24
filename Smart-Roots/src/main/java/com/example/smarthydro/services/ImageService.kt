package com.example.smarthydro.services

import com.example.smarthydro.models.ImageModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

private const val BASE_URL = "https://smart-roots-server.onrender.com"

object ImageService {
    private val retrofitService by lazy{
        Retrofit
            .Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ImageApi::class.java)
    }
    fun retrofitClient(): ImageApi{
        return retrofitService
    }
}

interface ImageApi{
    @GET("/api/images")
   suspend fun getLatestImage(): ImageModel
}

