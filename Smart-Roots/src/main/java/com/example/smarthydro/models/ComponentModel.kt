package com.example.smarthydro.models

import com.google.gson.annotations.SerializedName

/**
 * - We could have used the sensor model but to keep this
 *   more in context, I created a separate model with a single purpose.
 *  - For context these will send 1 if they need to be toggled
 *  0 if they stay the same.
 */
data class ComponentModel(
    @SerializedName("EC")
    val eC: Int = 0,
    @SerializedName("ExtractorFan")
    val extractorFan: Int = 0,
    @SerializedName("Light")
    val light:  Int = 0,
    @SerializedName("pH")
    val pH:  Int = 0,
    @SerializedName("Fan")
    val fan:  Int = 0,
    @SerializedName("Pump")
    val pump:  Int = 0
)
