package com.example.smarthydro.models

import com.google.gson.annotations.SerializedName

data class TentModel (
    @SerializedName("mac_address")
    val macAddress:String = "",
    @SerializedName("tent_name")
    val tentName:String = "",
    @SerializedName("location")
    val tentLocation:String = "",
    @SerializedName("country")
    val country:String = "",
    @SerializedName("tent_type")
    val tentType:String ="",
    @SerializedName("organization_name")
    val organizationName:String = ""
)