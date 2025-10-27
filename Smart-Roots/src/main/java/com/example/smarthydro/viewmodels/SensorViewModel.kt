package com.example.smarthydro.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    companion object {
        private const val TAG = "SensorViewModel"
    }

    fun fetchSensorData() {
        Log.d(TAG, "fetchSensorData() called")
        viewModelScope.launch {
            try {
                Log.d(TAG, "Attempting to fetch local sensor data from repository")
                val data = repository.getSensorData()
                Log.d(TAG, "Local sensor data fetched successfully: $data")

                isLocal = true
                Log.d(TAG, "isLocal flag set to $isLocal")

                _sensorData.value = data
                Log.d(TAG, "LiveData updated with local sensor data")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching local sensor data", e)
            } finally {
                Log.d(TAG, "fetchSensorData() execution finished")
            }
        }
    }

    fun fetchRemoteSensorData(timeoutMillis: Long = 5000, macAddress: String) {
        Log.d(TAG, "fetchRemoteSensorData() called")
        viewModelScope.launch {
            var gotData = false
            try {
                repository.getRemoteSensorData { reading ->
                    Log.d(TAG, "New remote reading received: $reading")

                    _sensorData.postValue(
                        SensorModel(
                            eC = reading.eC ?: "0",
                            humidity = reading.humidity ?: "0",
                            light = reading.light ?: "0",
                            pH = reading.pH ?: "0",
                            temperature = reading.temperature ?: "0",
                            flowRate = reading.flowRate ?: "0"
                        )
                    )
                    Log.d(TAG, "LiveData updated with remote sensor reading")

                    isLocal = false
                    gotData = true
                    Log.d(TAG, "isLocal flag set to $isLocal (remote)")
                }

                delay(timeoutMillis)

                if (!gotData) {
                    isLocal = true
                    Log.d(TAG, "No remote data received, reverting isLocal to true")
                    fetchSensorData()
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching remote sensor data", e)
                isLocal = true
                Log.d(TAG, "Error occurred, reverting isLocal to true")
                fetchSensorData()
            } finally {
                Log.d(TAG, "fetchRemoteSensorData() execution finished")
            }
        }
    }

    fun fetchSensorPeriodically(milliseconds: Long) {
        Log.d(TAG, "fetchSensorPeriodically() called with interval: $milliseconds ms")
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                Log.d(TAG, "Periodic fetch cycle started")
                fetchSensorData()
                Log.d(TAG, "Delaying next fetch for $milliseconds ms")
                delay(milliseconds)
            }
            Log.d(TAG, "fetchSensorPeriodically() coroutine is no longer active")
        }
    }
}
