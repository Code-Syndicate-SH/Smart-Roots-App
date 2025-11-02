package com.example.smarthydro.ui.theme.screen.note

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smarthydro.R
import com.example.smarthydro.domain.HapticFeedback
import com.example.smarthydro.ui.theme.AutoBlue
import com.example.smarthydro.ui.theme.DeepBlue
import com.example.smarthydro.ui.theme.SO_OnSurf_D
import com.example.smarthydro.ui.theme.SO_Surf_D
import leagueSpartan


@Composable
fun NoteScreen(navController: NavController, context: Context, padding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = DeepBlue) // Themed background
            .padding(padding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(50.dp))


        Spacer(modifier = Modifier.height(40.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp) // Manages space between buttons
        ) {
            ThemedNoteButton(
                text = "New Entry",
                icon = {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "New Entry",
                        modifier = Modifier.size(64.dp),
                        tint = Color.White
                    )
                },
                containerColor = AutoBlue,
                contentColor = Color.White,
                onClick = {
                    val hapticFeedback = HapticFeedback()
                    hapticFeedback(context)
                    navController.navigate("WriteToNote")
                }
            )

            ThemedNoteButton(
                text = "My Notes",
                icon = {
                    Icon(
                        imageVector = Icons.Default.ListAlt,
                        contentDescription = "My Notes",
                        modifier = Modifier.size(64.dp),
                        tint = SO_OnSurf_D
                    )
                },
                containerColor = SO_Surf_D,
                contentColor = SO_OnSurf_D,
                border = BorderStroke(1.dp, AutoBlue.copy(alpha = 0.6f)),
                onClick = {
                    navController.navigate("ViewNotes")
                }
            )
        }
    }
}

@Composable
private fun ThemedNoteButton(
    text: String,
    icon: @Composable () -> Unit,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    border: BorderStroke? = null
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp), // Adjusted height for a cleaner look
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        border = border,
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            icon()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = text,
                fontSize = 32.sp,
                fontFamily = leagueSpartan,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}