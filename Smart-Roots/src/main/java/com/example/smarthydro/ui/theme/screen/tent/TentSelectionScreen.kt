package com.example.smarthydro.ui.theme.screen.tent

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smarthydro.Destination
import com.example.smarthydro.R
import com.example.smarthydro.models.TentModel
import com.example.smarthydro.viewmodels.TentViewModel
import leagueSpartan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TentSelectionScreen(
    navController: NavController,
    filterType: String,
    tentViewModel: TentViewModel = viewModel(),
    paddingValues: PaddingValues,
) {
    val state by tentViewModel.tentManagementState.collectAsState()

    val filteredTents = remember(state.tents, filterType) {
        state.tents.filter { it.tentType.equals(filterType, ignoreCase = true) }
    }

    LaunchedEffect(Unit) {
        tentViewModel.loadAllTents()
    }

    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else if (filteredTents.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No ${filterType.lowercase()} tents found.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),

                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
            ) {
                items(filteredTents, key = { it.macAddress }) { tent ->
                    TentListTile(
                        tent = tent,
                        onClick = {
                            tentViewModel.currentTent(tent)
                            navController.navigate(
                                Destination.DashboardWithMac.createRoute(tent.macAddress)
                            )
                        }
                    )
                }
            }

        }
    }
}


@Composable
private fun TentListTile(
    tent: TentModel,
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
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Main Icon
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Default.HomeWork,
                        contentDescription = "Tent",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(Modifier.size(16.dp))

            // Details Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Tent Name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        // Assumes a drawable resource R.drawable.name exists
                        painter = painterResource(id = R.drawable.name),
                        contentDescription = "Tent Name",
                        modifier = Modifier.size(18.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = tent.tentName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = leagueSpartan
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Organization Name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = "Organization",
                        modifier = Modifier.size(18.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = tent.organizationName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Location Name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        // Assumes a drawable resource R.drawable.location exists
                        painter = painterResource(id = R.drawable.location),
                        contentDescription = "Location",
                        modifier = Modifier.size(18.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = tent.tentLocation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Navigation Arrow
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}