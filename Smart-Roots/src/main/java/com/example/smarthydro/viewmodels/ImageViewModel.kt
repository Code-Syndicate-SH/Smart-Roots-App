package com.example.smarthydro.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthydro.repositories.ImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "Image"
private const val FETCH_INTERVAL_MS = 5 * 60 * 1000L // 5 minutes

class ImageViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ImageUIState())
    val uiState: StateFlow<ImageUIState> = _uiState.asStateFlow()

    private val repository: ImageRepository = ImageRepository()

    init {
        startPeriodicFetch()
    }

    /** Starts a coroutine that fetches the latest image every 5 minutes */
    private fun startPeriodicFetch() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                fetchLatestImage()
                delay(FETCH_INTERVAL_MS)
            }
        }
    }

    /** Fetch the latest image once */
    private fun fetchLatestImage() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val imageUrl = repository.fetchLatestImage().url
                if (imageUrl.isBlank()) {
                    updateErrorMessage(
                        "No images from ${_uiState.value.tentName} at location ${_uiState.value.tentLocation}."
                    )
                    return@launch
                }
                updateImage(imageUrl)
            } catch (ex: Exception) {
                updateErrorMessage("There was an error fetching the most recent image.")
                Log.e(TAG, "Error trying to load image", ex)
            }
        }
    }

    /** Update tent information */
    fun updateTentInformation(tentLocation: String, tentName: String) {
        _uiState.update {
            it.copy(tentLocation = tentLocation, tentName = tentName)
        }
    }

    /** Update UI with an error message */
    private fun updateErrorMessage(errorMessage: String) {
        _uiState.update {
            it.copy(errorMessage = errorMessage, imageUrl = "")
        }
    }

    /** Update UI with the latest image */
    private fun updateImage(url: String) {
        _uiState.update {
            it.copy(imageUrl = url, lastUpdated = System.currentTimeMillis(), errorMessage = "")
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
