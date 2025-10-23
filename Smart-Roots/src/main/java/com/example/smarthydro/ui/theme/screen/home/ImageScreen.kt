package com.example.smarthydro.ui.theme.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.smarthydro.viewmodels.ImageViewModel

@Composable
fun ImageScreen(
    imageViewModel: ImageViewModel,
    onBack: () -> Unit
) {
    val uiState by imageViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Latest image from ${uiState.tentName} at ${uiState.tentLocation}")

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.imageUrl.isNotBlank()) {
            ImageContainer(lastTaken = uiState.lastUpdated, url = uiState.imageUrl)
        } else {
            Text(uiState.errorMessage.ifBlank { "Fetching image..." })
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onBack) {
            Text("Back")
        }
    }
}

@Composable
fun ImageContainer(lastTaken: Long, url: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AsyncImage(
            model = url,
            contentDescription = "Last taken image from the tent",
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Last updated: ${java.text.SimpleDateFormat("HH:mm:ss").format(lastTaken)}")
    }
}
