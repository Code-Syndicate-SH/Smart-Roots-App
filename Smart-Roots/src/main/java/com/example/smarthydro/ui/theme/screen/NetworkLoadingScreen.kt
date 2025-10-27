package com.example.smarthydro.ui.theme.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smarthydro.services.RemoteClientPing
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

// Represents the state of the network check
private sealed interface NetworkCheckState {
    object Loading : NetworkCheckState
    object Online : NetworkCheckState
    object Offline : NetworkCheckState
}

@Composable
fun NetworkLoadingScreen(
    onOnlineDetected: () -> Unit,
    onOfflineDetected: () -> Unit
) {
    var networkState by remember { mutableStateOf<NetworkCheckState>(NetworkCheckState.Loading) }

    // This effect triggers the network check when the screen is first composed
    LaunchedEffect(key1 = true) {
        val client = RemoteClientPing.httpClient
        val url = "https://smart-roots-server.onrender.com/"
        try {

            val response: HttpResponse = withContext(Dispatchers.IO) {
                client.get(url)
            }

            networkState = if (response.status.value in 200..299) {
                NetworkCheckState.Online
            } else {
                NetworkCheckState.Offline
            }
        } catch (e: Exception) {

            networkState = NetworkCheckState.Offline
        }
    }


    LaunchedEffect(networkState) {
        when (networkState) {
            NetworkCheckState.Online -> {
                delay(800L)
                onOnlineDetected()
            }
            NetworkCheckState.Offline -> {
                delay(800L)
                onOfflineDetected()
            }
            NetworkCheckState.Loading -> {}
        }
    }

    // The UI for the loading screen
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Smart Hydro",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(64.dp))

        // Use AnimatedContent for a smooth transition between states
        AnimatedContent(targetState = networkState, label = "NetworkStatusAnimation") { state ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (state) {
                    NetworkCheckState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary, // Poppy green
                            strokeWidth = 5.dp
                        )
                        Text(
                            text = "Checking connection...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    NetworkCheckState.Online -> {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = "Online",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary // Poppy green
                        )
                        Text(
                            text = "Connected to cloud!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    NetworkCheckState.Offline -> {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = "Offline",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Local network detected.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}