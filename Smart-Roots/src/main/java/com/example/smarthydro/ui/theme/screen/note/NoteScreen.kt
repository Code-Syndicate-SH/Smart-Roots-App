package com.example.smarthydro.ui.theme.screen.note

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.size.Size
import com.example.smarthydro.R
import com.example.smarthydro.domain.HapticFeedback
import leagueSpartan

@Composable
fun NoteScreen(
    navController: NavController,
    context: Context
) {
    val cs = MaterialTheme.colorScheme
    val lc = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Header chip (Notes • Open) like the video
        Surface(
            color = cs.surface,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = cs.secondary,
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {}
                Spacer(Modifier.size(12.dp))
                Column {
                    Text(
                        text = "Notes",
                        color = cs.onSurface.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Open",
                        color = cs.onSurface,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Two big cards: "New Entry" and "My Notes"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // New Entry (keeps your haptics + GIF background)
            Button(
                onClick = {
                    HapticFeedback()(context)
                    navController.navigate("WriteToNote")
                },
                modifier = Modifier
                    .weight(1f)
                    .height(160.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = cs.primary,
                    contentColor = cs.onPrimary
                )
            ) {
                Box(Modifier.fillMaxSize()) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = ImageRequest.Builder(lc)
                                .data(R.drawable.buttonation) // your animated asset
                                .decoderFactory(
                                    // use ImageDecoder on API 28+, else GifDecoder
                                    if (android.os.Build.VERSION.SDK_INT >= 28)
                                        ImageDecoderDecoder.Factory()
                                    else GifDecoder.Factory()
                                )
                                .size(Size.ORIGINAL)
                                .build()
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = "New Entry",
                        fontFamily = leagueSpartan,
                        fontSize = 28.sp,
                        color = cs.onPrimary,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            // My Notes
            Button(
                onClick = { navController.navigate("ViewNotes") },
                modifier = Modifier
                    .weight(1f)
                    .height(160.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = cs.tertiary,
                    contentColor = cs.onTertiary
                )
            ) {
                Box(Modifier.fillMaxSize()) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = ImageRequest.Builder(lc)
                                .data(R.drawable.buttonview) // your animated/video-like background
                                .decoderFactory(
                                    if (android.os.Build.VERSION.SDK_INT >= 28)
                                        ImageDecoderDecoder.Factory()
                                    else GifDecoder.Factory()
                                )
                                .size(Size.ORIGINAL)
                                .build()
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = "My Notes",
                        fontFamily = leagueSpartan,
                        fontSize = 28.sp,
                        color = cs.onTertiary,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}