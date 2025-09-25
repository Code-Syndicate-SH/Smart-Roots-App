package com.example.smarthydro.ui.theme.analyzer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.smarthydro.domain.AgeClassifier
import com.example.smarthydro.domain.Classification
import java.io.ByteArrayOutputStream

/**
 * This analyzer will get called for each frame
 */
class ImageAnalyzer(
    private val classifier: AgeClassifier,
    private val onResults: (List<Classification>) -> Unit,
) : ImageAnalysis.Analyzer {
    private var frameSkipCounter = 0

    /**
     * - Retrieves the image rotation degrees.
     * - Converts the input `ImageProxy` to a `Bitmap`.
     * - Crops the `Bitmap` to a 321x321 center square.
     * - Classifies the cropped `Bitmap` using the `classifier`.
     * - Invokes the `onResults` callback with the classification results.
     * - Closes the `ImageProxy`.
     * Additionally, a `frameSkipCounter` has been introduced
     * to process only every 60th frame, optimizing performance.
     */
    override fun analyze(image: ImageProxy) {
        if (frameSkipCounter % 60 == 0) {
            val rotationDegrees = image.imageInfo.rotationDegrees
            val bitmap = image.toBitmap().centerCrop(321, 321)
            val results = classifier.classify(bitmap, rotationDegrees)
            onResults(results)

        }
        frameSkipCounter++
        image.close()
    }

    /**
     * - Extracts Y, U, and V byte buffers from the `ImageProxy` planes.
     * - Combines these buffers into a single `ByteArray` in NV21 format.
     * - Creates a `YuvImage` from the NV21 data.
     * - Compresses the `YuvImage` to JPEG format.
     * - Decodes the JPEG byte array into a `Bitmap`.
     *
     */
    fun ImageProxy.toBitmap(): Bitmap {
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
        val jpegBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
    }
}



