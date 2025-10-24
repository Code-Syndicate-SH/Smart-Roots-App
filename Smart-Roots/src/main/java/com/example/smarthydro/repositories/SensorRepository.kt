package com.example.smarthydro.repositories

import androidx.lifecycle.MutableLiveData
import com.example.smarthydro.domain.SensorStreamClient
import com.example.smarthydro.models.RemoteSensorModel
import com.example.smarthydro.models.SensorModel
import com.example.smarthydro.services.SensorService

class SensorRepository {
    private val sensorService = SensorService.buildService()

    private val sensorService2 = SensorService.buildService2()
    suspend fun getSensorData(): SensorModel {
        return sensorService.getSensorData()
    }
    //the second url's method is defined
    suspend fun getRemoteSensorData(callback:(RemoteSensorModel)->Unit): SensorStreamClient {
        return SensorService.sensorStream(callback)
    }
}