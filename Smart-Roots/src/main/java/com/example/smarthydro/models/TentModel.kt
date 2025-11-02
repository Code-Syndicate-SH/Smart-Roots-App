package com.example.smarthydro.models

import com.google.gson.annotations.SerializedName

data class TentModel (
    @SerializedName("macAddress")
    val macAddress:String = "",
    @SerializedName("name")
    val tentName:String = "",
    @SerializedName("location")
    val tentLocation:String = "",
    @SerializedName("country")
    val country:String = "",
    @SerializedName("tentType")
    val tentType:String ="",
    @SerializedName("organizationName")
    val organizationName:String = ""
)