package com.example.smarthydro.data

import android.content.Context
import android.graphics.Bitmap
import com.example.smarthydro.domain.AgeClassifier
import com.example.smarthydro.domain.Classification
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.InterpreterApi
import java.nio.ByteBuffer
import java.nio.ByteOrder


/**
 * Starting from a minimum score of 0.5, we begin classification
 * matching how strong the content relates to the trained model
 * @param context:Context
 * @param threshold:Float
 */
class TfLiteAgeClassifier(
    private val context: Context,
) : AgeClassifier {

    private var interpreter: InterpreterApi? = null
    private val imgSize = 128
    private val numClasses = 9

    init {
        interpreter = Interpreter(loadModelFile("age_model.tflite"))
    }


    override fun classify(bitmap: Bitmap, rotation: Int, topK: Int): List<Classification> {
        val input = preprocess(bitmap)
        val output = Array(1) { FloatArray(numClasses) }

        interpreter?.run(input, output)

        val scores = output[0]

        return scores
            .mapIndexed { idx, score ->
                Classification(
                    age = idx,
                    age_class = mapAgeClass(idx),
                    score = score
                )
            }
            .filter { it.score >= 0.85f }
            .sortedByDescending { it.score }
            .take(topK)
    }

    private fun preprocess(bitmap: Bitmap): ByteBuffer {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, imgSize, imgSize, true)
        val inputBuffer = ByteBuffer.allocateDirect(4 * imgSize * imgSize * 3) // float32
        inputBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(imgSize * imgSize)
        scaledBitmap.getPixels(pixels, 0, imgSize, 0, 0, imgSize, imgSize)

        var pixelIndex = 0
        for (y in 0 until imgSize) {
            for (x in 0 until imgSize) {
                val pixel = pixels[pixelIndex++]

                val r = ((pixel shr 16) and 0xFF) / 255.0f
                val g = ((pixel shr 8) and 0xFF) / 255.0f
                val b = (pixel and 0xFF) / 255.0f

                inputBuffer.putFloat(r)
                inputBuffer.putFloat(g)
                inputBuffer.putFloat(b)
            }
        }
        return inputBuffer
    }

    private fun loadModelFile(filename: String): ByteBuffer {
        val assetFileDescriptor = context.assets.openFd(filename)
        val inputStream = assetFileDescriptor.createInputStream()
        val fileBytes = ByteArray(assetFileDescriptor.declaredLength.toInt())
        inputStream.read(fileBytes)
        inputStream.close()

        val buffer = ByteBuffer.allocateDirect(fileBytes.size)
        buffer.order(ByteOrder.nativeOrder())
        buffer.put(fileBytes)
        buffer.rewind()
        return buffer
    }

    private fun mapAgeClass(bucket: Int): String {
        return when (bucket) {
            0 -> "0–9"
            1 -> "10–19"
            2 -> "20–29"
            3 -> "30–39"
            4 -> "40–49"
            5 -> "50–59"
            6 -> "60–69"
            7 -> "70–79"
            8 -> "80–89"
            else -> "Unknown"
        }
    }
}