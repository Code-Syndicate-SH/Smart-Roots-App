package com.example.smarthydro.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthydro.models.RemoteSensorModel
import com.example.smarthydro.models.SensorModel
import com.example.smarthydro.repositories.SensorRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SensorViewModel : ViewModel() {
    private val repository = SensorRepository()

    private val _sensorData = MutableLiveData(SensorModel())
    val sensorData: LiveData<SensorModel> = _sensorData
    var isLocal = false

    fun fetchSensorData() {
        viewModelScope.launch {
            try {
                val data = repository.getSensorData()
                isLocal = true
                _sensorData.value = data
            } catch (e: Exception) {
                Log.e("SENSOR ERROR", e.message.toString())
            }
            //The second url has been used to get the data in the following try catch block - 2024
            // now we are making use of the server but still displaying the data in the same manner - 2025

        }

    }
   fun fetchRemoteSensorData(){
       viewModelScope.launch {
           try {

              repository.getRemoteSensorData(  {
                       reading ->

                   Log.d("SSE", "New reading: $reading")
                   _sensorData.postValue( SensorModel(
                       eC = reading.eC ?: "0",
                       humidity = reading.humidity ?: "0",
                       light = reading.light ?: "0",
                       pH = reading.pH ?: "0",
                       temperature = reading.temperature ?: "0",
                       flowRate = reading.flowRate ?: "0"
                   ))


                   // Update LiveData, Compose state, etc.
               })
               isLocal = false

           } catch (e: Exception) {
               Log.e("SENSOR ERROR", e.message.toString())
           }
       }
    }

    fun fetchSensorPeriodically(milliseconds: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                fetchSensorData()
                delay(milliseconds)
            }
        }
    }
}