package com.example.smarthydro.ui.theme.screen.home

import android.content.Context
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.smarthydro.data.TfLiteAgeClassifier
import com.example.smarthydro.domain.Classification
import com.example.smarthydro.ui.theme.analyzer.ImageAnalyzer


@Composable
fun AgeCameraScreen(context: Context, navigateToHomeScreen:()->Unit ) {
    var classifications by remember { mutableStateOf(emptyList<Classification>()) }
    val analyzer = remember {
        ImageAnalyzer(
            classifier = TfLiteAgeClassifier(
                context = context
            ),
            onResults = {
                classifications = it
            }
        )
    }

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
            setImageAnalysisAnalyzer(ContextCompat.getMainExecutor(context), analyzer)
        }
    }
    LaunchedEffect(classifications.isNotEmpty()) {
        if(classifications[0].age>0){
            navigateToHomeScreen()
        }
    }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AgeCameraPreview(controller = cameraController, modifier = Modifier.fillMaxSize())
        Column(
            Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            classifications.forEach {
                Text(
                    text = it.age_class + " " + it.score, modifier = Modifier
                        .width(300.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer
                        )
                        .padding(8.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )

            }
        }
    }
}

@Composable
fun AgeCameraPreview(modifier: Modifier, controller: LifecycleCameraController) {
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