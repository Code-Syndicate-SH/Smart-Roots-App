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
        // When we start loading, explicitly set the loading state to true
        _tentManagement.update { it.copy(isLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val listOfTents = repository.getAllTents()
                Log.d("Tents", listOfTents.count().toString())
                // When finished, update the list AND set loading to false
                _tentManagement.update {
                    it.copy(
                        tents = listOfTents,
                        isLoading = false
                    )
                }
            } catch (exception: Exception) {
                Log.e(TAG, "Error fetching current tents", exception)
                // IMPORTANT: Also set loading to false on error, so the spinner doesn't get stuck
                _tentManagement.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load tents."
                    )
                }
            }
        }
    }

    // This function is no longer needed here as the logic is handled in loadAllTents
    // fun updateTentList(tents: List<TentModel>) {
    //     _tentManagement.update {
    //         it.copy(tents = tents)
    //     }
    // }
}

data class TentManagementState(
    val tents: List<TentModel> = emptyList(),
    val errorMessage: String = "",
    // Add an isLoading flag, defaulting to true so it shows loading initially
    val isLoading: Boolean = true
)

data class TentUIState(
    val macAddress: String = "",
    val country: String = "",
    val organizationName: String = "",
    val tentType: String = "",
    val location: String = "",
)