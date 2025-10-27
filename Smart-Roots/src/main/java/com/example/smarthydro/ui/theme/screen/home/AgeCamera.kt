package com.example.smarthydro.ui.theme.screen.home

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.smarthydro.data.TfLiteAgeClassifier
import com.example.smarthydro.domain.Classification
import com.example.smarthydro.services.RemoteClientPing
import com.example.smarthydro.ui.theme.analyzer.AgeImageAnalyzer
import com.example.smarthydro.ui.theme.analyzer.FaceImageAnalyzer
import com.google.mlkit.vision.face.Face
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun AgeCameraScreen(context: Context, navigateToHomeScreen: () -> Unit) {
    var classifications by remember { mutableStateOf(emptyList<Classification>()) }
    var faces by remember { mutableStateOf(emptyList<Face>()) }
    val ageImageAnalyzer = remember {
        AgeImageAnalyzer(
            classifier = TfLiteAgeClassifier(
                context = context
            ),
            onAgeResults = {
                classifications = it
            },

            )
    }
    val faceImageAnalyzer = remember {
        FaceImageAnalyzer(
            ageAnalyzer = ageImageAnalyzer
        )
    }

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
            setImageAnalysisAnalyzer(ContextCompat.getMainExecutor(context), faceImageAnalyzer)
        }
    }

    LaunchedEffect(classifications.count()>0) {
        var hasClassified  = false
        for (classification in classifications) {
            if (classifications.size > 0 && classification.age > 0 && classification.score > 0.20f) {
                hasClassified =true
                delay(2000)

            }
        }
        var hasInternet = false
        val connectionStatus = RemoteClientPing.httpClient
        CoroutineScope(Dispatchers.IO).launch {
            val response: HttpResponse =
                connectionStatus.get("https://smart-roots-server.onrender.com")
            delay(1500)
            if (response.status.value in 200..299) {
                hasInternet = true
            }else{
                navigateToHomeScreen()
            }
        }
    }
        var displayText = when {
            classifications.isEmpty() -> "No faces detected, please face the camera"
            classifications[0].age >= 0 -> "Please hold still, almost there"
            else -> "Unknown objects detected, face the camera and hold still"
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Smart Roots",
                fontStyle = FontStyle.Companion.Italic,
                fontSize = 40.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                modifier = Modifier,
                color = Color.White
            )

            AgeCameraPreview(
                controller = cameraController,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .fillMaxHeight(0.6f)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Color.White
                    ), contentAlignment = Alignment.BottomCenter
            ) {
                if(classifications.size>0){
                    Text(
                        text = classifications[0].age_class, modifier = Modifier
                            .width(200.dp)
                            .padding(8.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                }else{
                    Text(
                        text = displayText, modifier = Modifier
                            .width(300.dp)
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
        var cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
            .build()
        AndroidView(
            factory = {
                PreviewView(it).apply {
                    this.controller = controller
                    this.controller?.cameraSelector = cameraSelector
                    controller.bindToLifecycle(lifecycleOwner)

                }
            },
            modifier = modifier.clip(RoundedCornerShape(10.dp))
        )


    }

