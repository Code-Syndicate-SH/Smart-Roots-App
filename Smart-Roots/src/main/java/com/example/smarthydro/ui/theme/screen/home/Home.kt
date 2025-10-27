package com.keagan.smartroots.screens // Or your actual package

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.navigation.NavController
import com.example.smarthydro.Destination // Assuming this is your navigation destination
import com.example.smarthydro.R
import leagueSpartan

@Composable
fun HomeScreen(
    navController: NavController,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Language selector, now with SharedPreferences logic
        LanguageSelector()

        Image(
            painter = painterResource(id = R.drawable.greeting),
            contentDescription = "Greeting",
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            HomeListTile(
                icon = Icons.Default.LocalFlorist,
                title = "Vegetables",
                onClick = { navController.navigate(Destination.Dashboard.route) }
            )
            HomeListTile(
                icon = Icons.Default.Grass,
                title = "Fodder",
                onClick = { navController.navigate(Destination.Dashboard.route) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * A reusable list tile component with the same style as the Dashboard's MetricListTile.
 */
@Composable
private fun HomeListTile(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 18.dp), // Increased vertical padding for better spacing
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.size(12.dp))

            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = leagueSpartan
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Icon(
                imageVector = Icons.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}


/**
 * A self-contained language selector that reads from and writes to SharedPreferences.
 */
@Composable
private fun LanguageSelector(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

    var language by remember {
        mutableStateOf(sharedPreferences.getString("language_code", "EN") ?: "EN")
    }

    // This effect runs whenever 'language' changes, saving the new value.
    LaunchedEffect(language) {
        sharedPreferences.edit().putString("language_code", language).apply()
    }

    var expanded by remember { mutableStateOf(false) }
    val languages = listOf(
        "EN" to "English", "ZU" to "Zulu", "AF" to "Afrikaans", "XH" to "Xhosa",
        "ST" to "Sesotho", "TN" to "Setswana", "SS" to "siSwati", "VE" to "Tshivenda",
        "TS" to "Xitsonga", "NS" to "Northern Sotho", "ND" to "Ndebele"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(end = 16.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .border(
                    BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp)
                )
                .clip(RoundedCornerShape(8.dp))
        ) {
            Text(
                text = languages.find { it.first == language }?.second ?: "English",
                modifier = Modifier
                    .clickable { expanded = true }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.onBackground
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = true)
            ) {
                languages.forEach { (code, label) ->
                    DropdownMenuItem(
                        onClick = {
                            language = code
                            expanded = false
                        },
                        text = { Text(text = label) }
                    )
                }
            }
        }
    }
}