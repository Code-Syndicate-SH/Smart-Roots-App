package com.example.smarthydro.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

/**
 * @author
 * This model will be used to fetch the url from the server, which is an image from Supabase
 */

data class ImageModel(
    @SerializedName("Url")
    val url:String = ""
)