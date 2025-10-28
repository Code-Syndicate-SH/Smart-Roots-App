package com.example.smarthydro.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthydro.models.ComponentModel
import com.example.smarthydro.repositories.ComponentRepository
import kotlinx.coroutines.launch

class ComponentViewModel : ViewModel() {
    private val repository = ComponentRepository()


    fun setLight(macAddress: String? = null) {
        viewModelScope.launch {
            try {
                Log.d("ComponentViewModel", "Trying to toggle")
                repository.toggleLight()
            } catch (e: Exception) {
                Log.e("ComponentViewModel", e.message.toString())
            }

            try {
                var componentModel = ComponentModel(
                    light = 1
                )
                if (macAddress != null) {
                    repository.toggleComponent(componentModel = componentModel, macAddress = macAddress)
                }else{
                    Log.d("ComponentViewModel", "There is no data being passed")
                }

            } catch (e: Exception) {
                Log.e("ComponentViewModel", e.message.toString())
            }
        }
    }

    fun setPump(macAddress: String? = null) {
        viewModelScope.launch {
            try {
                repository.togglePump()
                Log.d("ComponentViewModel", "Trying to toggle")
            } catch (e: Exception) {
                Log.e("ComponentViewModel", e.message.toString())
            }
            //the second base url's call is set here
            try {
                var componentModel = ComponentModel(
                    pump = 1
                )
                if (macAddress != null) {
                    repository.toggleComponent(componentModel = componentModel, macAddress = macAddress)
                }

            } catch (e: Exception) {
                Log.e("ComponentViewModel", e.message.toString())
            }
        }
    }

    fun setExtractor(macAddress: String? = null) {
        viewModelScope.launch {
            try {
                repository.toggleExtractor()
            } catch (e: Exception) {
                Log.e("ComponentViewModel", e.message.toString())
            }
            //the second base url's call is set here
            try {
                var componentModel = ComponentModel(
                    extractorFan = 1
                )
                if (macAddress != null) {
                    repository.toggleComponent(componentModel = componentModel, macAddress = macAddress)
                }
            } catch (e: Exception) {
                Log.e("ComponentViewModel", e.message.toString())
            }
        }
    }

    fun setFan(macAddress: String? = null) {
        viewModelScope.launch {
            try {
                repository.toggleFan()

            } catch (e: Exception) {
                Log.e("ComponentViewModel", e.message.toString())
            }
            //the second base url's call is set here
            try {
                var componentModel = ComponentModel(
                    fan = 1
                )
                if (macAddress != null) {
                    repository.toggleComponent(componentModel = componentModel, macAddress = macAddress)
                }
            } catch (e: Exception) {
                Log.e("ComponentViewModel", e.message.toString())
            }
        }
    }


    fun setPhUp(macAddress: String? = null) {
        viewModelScope.launch {
            try {
                repository.phUp()
            } catch (e: Exception) {
                Log.e("ComponentViewModel", e.message.toString())
            }

//the second base url's call is set here
            try {
                var componentModel = ComponentModel(
                    pHUp = 1
                )
                if (macAddress != null) {
                    repository.toggleComponent(componentModel = componentModel, macAddress= macAddress)
                }
            } catch (e: Exception) {
                Log.e("ComponentViewModel", e.message.toString())
            }
        }
    }

    fun setPhDown(macAddress: String? = null) {
        viewModelScope.launch {
            try {
                repository.phDown()
            } catch (e: Exception) {
                Log.e("ComponentViewModel", e.message.toString())
            }
            //the second base url's call is set here
            try {
                var componentModel = ComponentModel(
                    pHDown = 1
                )
                if (macAddress != null) {
                    repository.toggleComponent(componentModel = componentModel, macAddress = macAddress)
                }
            } catch (e: Exception) {
                Log.e("PH ERROR", e.message.toString())
            }
        }
    }


    fun setEcUp(macAddress: String? = null) {
        viewModelScope.launch {
            try {
                repository.ecUp()
            } catch (e: Exception) {
                Log.e("EC ERROR", e.message.toString())
            }
            //the second base url's call is set here
            try {

                var componentModel = ComponentModel(
                    eCUp = 1
                )
                if (macAddress != null) {
                    repository.toggleComponent(componentModel = componentModel, macAddress = macAddress)
                }
            } catch (e: Exception) {
                Log.e("ComponentViewModel", e.message.toString())
            }
        }
    }

    fun setEcDown(macAddress: String? = null) {
        viewModelScope.launch {
            try {
                repository.ecDown()
            } catch (e: Exception) {
                Log.e("ComponentViewModel", e.message.toString())
            }
            //the second base url's call is set here
            try {

                var componentModel = ComponentModel(
                    eCDown = 1
                )
                if (macAddress != null) {
                    repository.toggleComponent(componentModel = componentModel, macAddress = macAddress)
                }
            } catch (e: Exception) {
                Log.e("ComponentViewModel", e.message.toString())
            }
        }
    }

}