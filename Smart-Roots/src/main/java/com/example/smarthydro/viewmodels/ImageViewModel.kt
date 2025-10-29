package com.example.smarthydro.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthydro.repositories.ImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
    private var periodicFetchJob: Job? = null // To manage the lifecycle of the loop

    init {
        // HIGHLIGHT #1: Start the single, managed loop when the ViewModel is created.
        startPeriodicFetch()
    }

    /**
     * This is the main function the UI should call.
     * It updates the state with the new macAddress and triggers an immediate fetch.
     */
    fun loadImagesForDevice(macAddress: String) {
        _uiState.update {
  
            it.copy(
                macAddress = macAddress,
                imageUrl = "",
                errorMessage = "",
            )
        }

        fetchLatestImage()
    }

    private fun startPeriodicFetch() {

        periodicFetchJob?.cancel()
        periodicFetchJob = viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                fetchLatestImage()
                delay(FETCH_INTERVAL_MS)
            }
        }
    }

    /**
     * Fetch the latest image once. It reads the macAddress from the UI state.
     * This function now takes no parameters.
     */
    private fun fetchLatestImage() {

        val currentMacAddress = _uiState.value.macAddress
        if (currentMacAddress.isBlank()) {
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val imageUrl = repository.fetchLatestImage(macAddress = currentMacAddress).url
                if (imageUrl.isBlank()) {
                    updateErrorMessage(
                        "No images available for this device."
                    )
                    return@launch
                }
                updateImage(imageUrl)
            } catch (ex: Exception) {
                updateErrorMessage("Error fetching the most recent image.")
                Log.e(TAG, "Error trying to load image for $currentMacAddress", ex)
            }
        }
    }

    private fun updateErrorMessage(errorMessage: String) {
        _uiState.update {
            it.copy(errorMessage = errorMessage, imageUrl = "")
        }
    }

    private fun updateImage(url: String) {
        _uiState.update {
            it.copy(imageUrl = url, lastUpdated = System.currentTimeMillis(), errorMessage = "")
        }
    }
}

data class ImageUIState(
    val macAddress: String = "",
    val imageUrl: String = "",
    val lastUpdated: Long = 0,

    val errorMessage: String = "",
)