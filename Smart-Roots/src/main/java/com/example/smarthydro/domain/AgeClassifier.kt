package com.example.smarthydro.domain

import android.graphics.Bitmap

interface AgeClassifier {
    /**
     * We use this to classify the image
     * @param bitmap:Bitmap
     * @param rotation:Int
     */
    fun classify(bitmap: Bitmap, rotation:Int):List<Classification>

}
