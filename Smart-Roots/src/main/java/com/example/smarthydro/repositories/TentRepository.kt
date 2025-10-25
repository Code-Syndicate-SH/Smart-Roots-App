package com.example.smarthydro.repositories

import com.example.smarthydro.models.TentModel
import com.example.smarthydro.services.TentService

class TentRepository {
    private val tentClient = TentService.returnTentClient()

    suspend fun getAllTents(): List<TentModel> {
        return tentClient.getAllTents()
    }

    suspend fun getTentWithMacAddress(macAddress: String) {
        return tentClient.getTentWithMacAddress(macAddress)
    }
}