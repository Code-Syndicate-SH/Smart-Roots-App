package com.example.smarthydro.ui.theme.screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.smarthydro.ui.theme.AutoBlue
import com.example.smarthydro.ui.theme.SO_OnSurf_D
import com.example.smarthydro.ui.theme.SO_SurfVar_D
import com.example.smarthydro.ui.theme.SO_Surf_D
import com.example.smarthydro.viewmodels.ImageViewModel
import java.text.SimpleDateFormat
import java.util.*
@Composable
fun ImageScreen(imageViewModel: ImageViewModel, ) {
    val uiState by imageViewModel.uiState.collectAsStateWithLifecycle()

    val placeholderTentName = "Tent A"
    val placeholderLocation = "Durban North"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SO_Surf_D)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "The latest image",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = SO_OnSurf_D
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // 👈 Take up all remaining space below the title
                .clip(RoundedCornerShape(20.dp))
                .background(SO_Surf_D)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Image section takes up 70% of the card height
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.7f)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .background(SO_SurfVar_D),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = uiState.imageUrl,
                            contentDescription = "Last taken image from the tent",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = "No image available yet.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = AutoBlue)
                        )
                    }
                }

                // Info panel below image
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.3f)
                        .background(SO_SurfVar_D)
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = placeholderTentName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = SO_OnSurf_D
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = placeholderLocation,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = SO_OnSurf_D.copy(alpha = 0.85f)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val formatted = if (uiState.lastUpdated > 0L) {
                        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(uiState.lastUpdated))
                    } else {
                        "unknown"
                    }

                    Text(
                        text = "Last updated: $formatted",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SO_OnSurf_D.copy(alpha = 0.7f)
                        )
                    )
                }
            }
        }
    }
}
