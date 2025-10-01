package com.example.smarthydro.repositories

import com.example.smarthydro.models.ComponentModel
import com.example.smarthydro.services.ComponentService


class ComponentRepository {
    private val componentService = ComponentService.buildService()
    private val remoteComponentService = ComponentService.remoteService()
    suspend fun toggleLight() {
        return componentService.toggleLight()
    }

    suspend fun togglePump() {
        return componentService.togglePump()
    }

    suspend fun toggleExtractor() {
        return componentService.toggleExtractor()
    }

    suspend fun ec() {
        return componentService.ec()
    }

    suspend fun ecUp() {
        return componentService.ecUp()
    }

    suspend fun ecDown() {
        return componentService.ecDown()
    }

    suspend fun ph() {
        return componentService.pH()
    }

    suspend fun phUp() {
        return componentService.pHUp()
    }

    suspend fun phDown() {
        return componentService.pHDown()
    }

    suspend fun toggleFan() {
        return componentService.toggleFan()
    }

    //The following functions are defined so that the second url can be used here - 2024
    // these have been removed in 2025
    suspend fun toggleComponent(componentModel: ComponentModel, id:String){
        return remoteComponentService.toggleComponent(componentModel = componentModel, id = id)
    }

}