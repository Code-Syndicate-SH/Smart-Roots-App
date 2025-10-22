package com.example.smarthydro.services

import com.example.smarthydro.domain.SensorStreamClient
import com.example.smarthydro.models.RemoteSensorModel
import com.example.smarthydro.models.SensorModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

private const val BASE_URL = "http://192.168.8.14/"

//The second url is declared below
private const val BASE_URL2 = "https://smart-roots-server.onrender.com"

object SensorService {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ISensors::class.java)
    }

    //The second url is set up to be used by retrofit
    private val retrofit2 by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL2)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ISensors::class.java)
    }

    fun buildService(): ISensors {
        return retrofit
    }

    //THe retrofit val is returned for the second url
    fun buildService2(): ISensors {
        return retrofit2
    }

    /**
     * @author Shravan Ramjathan
     * This was added in 2025 and this is a lighter weight version that is optimized to listen to live updates,
     * this is better then making get requests. Here it listens to a constant stream of data and fetches readings live.
     */
    fun sensorStream(callback: (RemoteSensorModel) -> Unit): SensorStreamClient {
        val  client  = SensorStreamClient(
            serverUrl = "https://smart-roots-server.onrender.com/api/sensors", // adjust to your endpoint
            onMessage = callback
        )
        client.start()
        return client
    }
}

object SensorStream {

}

interface ISensors {
    @GET("/r/n/r/n")
    suspend fun getSensorData(): SensorModel

    //The endpoint for the second url is declared

    @GET("getHistoricData")
    suspend fun getHistoricData(): SensorModel
}





