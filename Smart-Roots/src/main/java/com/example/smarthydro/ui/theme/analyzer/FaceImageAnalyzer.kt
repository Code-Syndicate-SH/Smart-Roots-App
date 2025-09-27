package com.example.smarthydro.ui.theme.analyzer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import java.io.ByteArrayOutputStream

class FaceImageAnalyzer(private val ageAnalyzer: AgeImageAnalyzer) :
    ImageAnalysis.Analyzer {

    private var lastAgeCheckTime = 0L
    private val ageCheckInterval = 2000L // run every 2 sec

    /**
     * - Gets the current rotation degrees of the image
     * - Takes the raw image from the proxy
     * - Gets the Client to initiate face detection
     * - Converts the image to a InputImage the face-detection can analyze with the rotation
     * - Listens to the state of the results
     * - Invokes the `onAgeResults` callback with the classification results.
     * - Closes the `ImageProxy`.
     * Additionally, a `frameSkipCounter` has been introduced
     * to process only every 60th frame, optimizing performance.
     */
    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(image: ImageProxy) {

        val rotationDegrees = image.imageInfo.rotationDegrees
        val bitmap = image.toBitmap()
        val mediaImage = image.image
        val detector = FaceDetection.getClient()
        if (mediaImage == null) {
            Log.d("faceAnalyzer", "The image coming in is null")
        }
        val inputImage = InputImage.fromMediaImage(mediaImage!!, rotationDegrees)
        detector.process(inputImage)
            .addOnSuccessListener {
                if (it.isNotEmpty()) {
                    val now = System.currentTimeMillis()
                    if (now - lastAgeCheckTime > ageCheckInterval) {
                        lastAgeCheckTime = now
                        val face = it[0]
                        val faceBitmap = Bitmap.createBitmap(
                            bitmap,
                            face.boundingBox.left.coerceAtLeast(0),
                            face.boundingBox.top.coerceAtLeast(0),
                            face.boundingBox.width()
                                .coerceAtMost(bitmap.width - face.boundingBox.left),
                            face.boundingBox.height()
                                .coerceAtMost(bitmap.height - face.boundingBox.top)
                        )
                        ageAnalyzer.analyze(faceBitmap, rotationDegrees)
                    }
                }
            }
            .addOnFailureListener {
                Log.e("faceAnalyzer", "Error scanning face", it)
            }
            .addOnCompleteListener {

                image.close()
            }


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

