package com.example.smarthydro.ui.theme.screen.home


import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.smarthydro.Destination
import com.example.smarthydro.R

import kotlinx.coroutines.delay

@Composable
fun AppSplashScreen(navController: NavController) {
    var visible by remember { mutableStateOf(false) }

    // Pulse animation for the logo
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Fade animation for whole screen
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(900)
    )

    LaunchedEffect(Unit) {
        visible = true
        delay(2500) // show splash for 2.5 seconds
        navController.navigate(Destination.AgeCamera.route) {
            popUpTo(Destination.SplashScreen.route) { inclusive = true }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            ,
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .alpha(alpha),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = null,
                modifier = Modifier
                    .size(130.dp)
                    .scale(scale)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "SmartRoots",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = if (MaterialTheme.colorScheme.background.luminance() > 0.5f)
                    Color.Black else Color.White
            )
            Text(
                "Hydroponics made simple",
                style = MaterialTheme.typography.bodyMedium,
                color = if (MaterialTheme.colorScheme.background.luminance() > 0.5f)
                    Color.Black.copy(alpha = 0.6f)
                else
                    Color.White.copy(alpha = 0.7f)
            )
        }
    }
}
