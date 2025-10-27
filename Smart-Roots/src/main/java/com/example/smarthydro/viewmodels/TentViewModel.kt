package com.example.smarthydro.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthydro.models.TentModel
import com.example.smarthydro.repositories.TentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TentViewModel : ViewModel() {
    private val TAG = "Tents"
    private val repository = TentRepository()
    private val _tentManagement = MutableStateFlow(TentManagementState())
    val tentManagementState = _tentManagement.asStateFlow()

    private val _tentUIState = MutableStateFlow(TentUIState())
    val tentUIState = _tentUIState.asStateFlow()
    fun loadAllTents() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val listOfTents = repository.getAllTents()
                updateTentList(listOfTents)
            } catch (exception: Exception) {
                Log.e(TAG, "Error fetching current tents", exception)
            }
        }
    }



    fun updateTentList(tents: List<TentModel>) {
        _tentManagement.update {
            it.copy(tents = tents)
        }
    }
}

data class TentManagementState(
    val tents: List<TentModel> = emptyList(),
    val errorMessage:String = ""
)

data class TentUIState(
    val macAddress: String = "",
    val country: String = "",
    val organizationName: String = "",
    val tentType: String = "",
    val location: String = "",
)