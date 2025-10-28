package com.example.smarthydro.ui.theme.screen.home

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.smarthydro.data.TfLiteAgeClassifier
import com.example.smarthydro.domain.Classification
import com.example.smarthydro.ui.theme.analyzer.AgeImageAnalyzer
import com.example.smarthydro.ui.theme.analyzer.FaceImageAnalyzer
import kotlinx.coroutines.delay


private enum class CameraUiState {
    Searching,
    Analyzing,
    Success
}

@Composable
fun AgeCameraScreen(navigateToLoadingScreen: () -> Unit) {

    val context = LocalContext.current

    var classifications by remember { mutableStateOf(emptyList<Classification>()) }
    var uiState by remember { mutableStateOf(CameraUiState.Searching) }
    val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

    var age by remember {
        mutableStateOf(sharedPreferences.getString("age_category", "0"))
    }
    if (age.toString().isNotEmpty() && age != "0") {
        navigateToLoadingScreen()
    }
    val ageImageAnalyzer = remember {
        AgeImageAnalyzer(
            classifier = TfLiteAgeClassifier(context = context),
            onAgeResults = { results ->
                if (results.isNotEmpty()) {
                    classifications = results

                    if (results.any { it.age > 0 }) {
                        sharedPreferences.edit()
                            .putString("age_category", results[0].age.toString()).apply()
                        uiState = CameraUiState.Success
                    } else {

                        uiState = CameraUiState.Analyzing
                    }
                } else {

                    uiState = CameraUiState.Searching
                }
            }
        )
    }
    val faceImageAnalyzer = remember { FaceImageAnalyzer(ageAnalyzer = ageImageAnalyzer) }

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
            setImageAnalysisAnalyzer(ContextCompat.getMainExecutor(context), faceImageAnalyzer)
            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA // Simplified selector
        }
    }


    LaunchedEffect(uiState) {
        if (uiState == CameraUiState.Success) {
            // Wait a moment so the user can see the success message
            delay(1500L)
            navigateToLoadingScreen()
        }
    }

    // --- UI Structure ---
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Title - Using MaterialTheme for typography
            Text(
                text = "Smart Roots", // Consider moving to string resources
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Camera Preview with a guiding frame
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .aspectRatio(3 / 4f) // Maintain a consistent aspect ratio
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        width = 4.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(24.dp)
                    )
            ) {
                AgeCameraPreview(
                    controller = cameraController,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Status Box with animated text
            StatusBox(
                uiState = uiState,
                classificationResult = classifications.firstOrNull()?.age_class ?: ""
            )
        }
    }
}

@Composable
private fun AgeCameraPreview(
    controller: LifecycleCameraController,
    modifier: Modifier = Modifier,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        factory = {
            PreviewView(it).apply {
                this.controller = controller
                controller.bindToLifecycle(lifecycleOwner)
            }
        },
        modifier = modifier
    )
}

@Composable
private fun StatusBox(
    uiState: CameraUiState,
    classificationResult: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth(0.8f)
            .height(100.dp) // Fixed height for stability
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        // --- UX Improvement: Animate text changes ---
        AnimatedContent(
            targetState = uiState,
            label = "StatusTextAnimation",
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            }
        ) { targetState ->
            val textToShow = when (targetState) {
                CameraUiState.Searching -> "Position your face in the frame"
                CameraUiState.Analyzing -> "Hold still, analyzing..."
                CameraUiState.Success -> "Success!\nAge detected: $classificationResult"
            }
            Text(
                text = textToShow,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}