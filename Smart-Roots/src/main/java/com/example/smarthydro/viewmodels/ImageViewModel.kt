package com.example.smarthydro.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthydro.repositories.ImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "Image"

class ImageViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ImageUIState())
    val uiState: StateFlow<ImageUIState> = _uiState.asStateFlow()
    private val repository: ImageRepository = ImageRepository()

    private fun fetchLatestImage() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val imageUrl = repository.fetchLatestImage().url
                if (imageUrl.isBlank() || imageUrl.isEmpty()) {
                    updateErrorMessage("No images from ${_uiState.value.tentName} at location ${_uiState.value.tentLocation}.")
                return@launch
                }
                updateImage(imageUrl)
            } catch (ex: Exception) {
              updateErrorMessage("There was an error fetching the most recent image.")
                Log.e(TAG, "Error trying to load image",ex)
            }
        }

    }
    private fun updateTentInformation(tentLocation:String, tentName:String){
        _uiState.update {
            it.copy(tentLocation = tentLocation, tentName = tentName)
        }
    }
    private fun updateErrorMessage(errorMessage: String) {
        _uiState.update {
            it.copy(imageUrl = errorMessage)
        }
    }

    private fun updateImage(url: String) {
        _uiState.update {
            it.copy(imageUrl = url)
        }
    }
}

data class ImageUIState(
    val imageUrl: String = "",
    val lastUpdated: Long = 0,

    val tentLocation: String = "",
    val tentName: String = "",

    val errorMessage: String = "",
)