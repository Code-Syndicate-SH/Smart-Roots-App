package com.keagan.smartroots.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.smarthydro.Destination

@Composable
private fun scrimForTheme(): Pair<Color, Color> {
    val isLight = MaterialTheme.colorScheme.background.luminance() > 0.5f
    val container = if (isLight) Color.White.copy(alpha = 0.88f)
    else Color(0xFF0E1A0E).copy(alpha = 0.70f)
    val content = if (isLight) Color.Black else Color.White
    return container to content
}

@Composable
private fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController// e.g., "veg", "fodder", "chatbot"
) {
    // Local preference-like states
    var isLightTheme by remember { mutableStateOf(true) }
    var languageTag by remember { mutableStateOf("EN") }

    val (scrimBg, scrimFg) = scrimForTheme()

    Surface(
        modifier = Modifier.fillMaxSize().systemBarsPadding(),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header
            Text(
                text = "Home",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = if (MaterialTheme.colorScheme.background.luminance() > 0.5f)
                    Color.Black else Color.White
            )

            Spacer(Modifier.height(20.dp))

            // Theme toggle
            Surface(color = scrimBg, contentColor = scrimFg, shape = MaterialTheme.shapes.medium) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Theme", fontWeight = FontWeight.SemiBold, color = scrimFg)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (isLightTheme) "Light" else "Dark", color = scrimFg)
                        Spacer(Modifier.width(8.dp))
                        Switch(
                            checked = isLightTheme,
                            onCheckedChange = { isLightTheme = it }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Language toggle
            Surface(color = scrimBg, contentColor = scrimFg, shape = MaterialTheme.shapes.medium) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Language", fontWeight = FontWeight.SemiBold, color = scrimFg)
                    Text(
                        languageTag,
                        color = scrimFg,
                        modifier = Modifier.clickable {
                            // Simple toggle for demonstration
                            languageTag = if (languageTag == "EN") "FR" else "EN"
                        }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Navigation buttons
            PrimaryActionButton(
                text = "Vegetables",
                onClick = { navController.navigate(Destination.Dashboard.route) },
                color = Color(0xFF2EBE7E)
            )
            Spacer(Modifier.height(14.dp))
            PrimaryActionButton(
                text = "Fodder",
                onClick = { navController.navigate(Destination.Dashboard.route) },
                color = Color(0xFFF39C12)
            )

        }
    }
}
