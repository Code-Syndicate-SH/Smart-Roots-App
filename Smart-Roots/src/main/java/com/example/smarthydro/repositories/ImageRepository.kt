package com.example.smarthydro.repositories

import com.example.smarthydro.models.ImageModel
import com.example.smarthydro.services.ImageService

/**
 * @author Shravan Ramjathan
 *
 */
class ImageRepository {
  private val imageService = ImageService.retrofitClient()
    suspend fun fetchLatestImage(macAddress: String): ImageModel{
        return imageService.getLatestImage(macAddress)
    }

}