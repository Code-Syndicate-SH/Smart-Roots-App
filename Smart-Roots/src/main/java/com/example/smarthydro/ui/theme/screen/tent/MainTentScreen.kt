package com.example.smarthydro.ui.theme.screen.tent

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun MainTentScreen(){
    LazyColumn {

        // THIS WILL BE NEARBY TENTS
    }
}

@Composable
fun TentCard(tentName:String, location:String, url:String, macAddress:String, onClick:(String)-> Unit){
    OutlinedCard {

        Text(location)
        Text(tentName, )
    }

}

@Composable
fun CardText(text:String){
    Row { Icon()
        Text(text )}

}
