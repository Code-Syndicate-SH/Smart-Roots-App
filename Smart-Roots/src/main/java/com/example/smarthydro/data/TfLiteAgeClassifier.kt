package com.example.smarthydro.data

import android.content.Context
import android.graphics.Bitmap
import android.view.Surface
import com.example.smarthydro.domain.AgeClassifier
import com.example.smarthydro.domain.Classification
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.core.vision.ImageProcessingOptions
import org.tensorflow.lite.task.vision.classifier.ImageClassifier


/**
 * Starting from a minimum score of 0.5, we begin classification
 * matching how strong the content relates to the trained model
 * @param context:Context
 * @param threshold:Float
 */
class TfLiteAgeClassifier(
    private val context: Context,
    private val threshold: Float = 0.5f,
    private val maxResults: Int = 1, // closest related Landmark, we could show more
) : AgeClassifier {

    private var classifier: ImageClassifier? = null

    /**
     * This sets up the base classifier with some base options
     * Using the .tflite file for reference
     */
    private fun setupClassifier() {
        val baseOptions = BaseOptions.builder()
            .setNumThreads(2)
            .build()
        val options = ImageClassifier.ImageClassifierOptions.builder()
            .setBaseOptions(baseOptions)
            .setMaxResults(maxResults)
            .setScoreThreshold(threshold)
            .build()

        try {
            classifier = ImageClassifier.createFromFileAndOptions(
                context,
                "age_model.tflite",
                options
            ) // here we passs in context and the file we use as a model
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    override fun classify(bitmap: Bitmap, rotation: Int): List<Classification> {
        if (classifier == null) {
            setupClassifier()
        }
        val imageProcessor = ImageProcessor.Builder().build()
        val tensorImage = imageProcessor.process(TensorImage.fromBitmap(bitmap))
        val imageProcessingOptions = ImageProcessingOptions.builder()
            .setOrientation(getOrientationFromRotation(rotation))
            .build()
        val results = classifier?.classify(tensorImage,imageProcessingOptions)
        return results?.flatMap { classifications->       // creates a list
            classifications.categories.map { category->   // maps each category that will be of type Classification
                Classification(name = category.displayName, score = category.score )
            }
        }?.distinctBy { it.name  }?:emptyList()  // makes sure there no duplicate for the category names as this model has duplicates
    }

    private fun getOrientationFromRotation(rotation: Int): ImageProcessingOptions.Orientation {
        return when (rotation) {
            Surface.ROTATION_270 -> ImageProcessingOptions.Orientation.BOTTOM_RIGHT
            Surface.ROTATION_90 -> ImageProcessingOptions.Orientation.TOP_LEFT
            Surface.ROTATION_180 -> ImageProcessingOptions.Orientation.RIGHT_BOTTOM
            else-> ImageProcessingOptions.Orientation.RIGHT_TOP
        }

    }
}