package com.example.smarthydro.ui.theme.screen.tent

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.smarthydro.R
import com.example.smarthydro.ui.theme.SO_SurfVar_D
import com.example.smarthydro.ui.theme.SO_Surf_D
import com.example.smarthydro.ui.theme.SO_Surf_L
import com.example.smarthydro.viewmodels.TentViewModel

@Composable
fun MainTentScreen(tentViewModel: TentViewModel, onClick: (String) -> Unit) {
    val tentManagementState by tentViewModel.tentManagementState.collectAsStateWithLifecycle()
    val tents = remember { tentManagementState.tents }
    val state = rememberLazyListState()
    LazyColumn(state = state, modifier = Modifier.fillMaxSize()) {
        items(tents) {tent->
            TentCard(
                tentName = tent.tentName,
                location = tent.tentLocation,
                macAddress = tent.macAddress,
                onClick = {onClick(tent.macAddress)})
        }
    }
}

@Composable
fun TentCard(tentName: String, location: String, macAddress: String, onClick: (String) -> Unit) {
    OutlinedCard(
        Modifier.size(width = 200.dp, height = 80.dp),
        border = BorderStroke(color = SO_Surf_D, width = 2.dp),
        colors = CardDefaults.cardColors(contentColor = SO_Surf_L, containerColor = SO_SurfVar_D)
    ) {
        CardText(tentName, R.drawable.name)

        // AsyncImage(model = url, contentDescription =  "", Modifier.size(100.dp))
        CardText(location, R.drawable.location)

    }

}

@Composable
fun CardText(text: String, resourceId: Int) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(id = resourceId),
            contentDescription = "",
            Modifier
                .size(25.dp)
                .padding(5.dp)
        )
        Text(text)
    }

}

@Preview
@Composable
fun myPreview() {
    TentCard("Name", "durban", "sdfsdf", onClick = {})

}