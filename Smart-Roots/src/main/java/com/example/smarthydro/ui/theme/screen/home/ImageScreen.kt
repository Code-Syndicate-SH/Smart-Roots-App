package com.example.smarthydro.ui.theme.screen.home

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

@Composable
fun ImageScreen(){
    Column {
        Text("The latest image")

    }
}

@Composable
fun ImageContainer(lastTaken:Long = 0, url:String, ){


    AsyncImage(model = url, contentDescription = "Last taken image from the tent"  )

}