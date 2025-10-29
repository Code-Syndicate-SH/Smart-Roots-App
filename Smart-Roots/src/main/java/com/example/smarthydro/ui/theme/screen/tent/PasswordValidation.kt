package com.example.smarthydro.ui.theme.screen.tent

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text2.BasicTextField2
import androidx.compose.foundation.text2.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PasswordDialog(inputState: TextFieldState, onConfirmation: (TextFieldState) -> Unit) {

    val openAlertDialog = remember { mutableStateOf(false) }


    when {

        openAlertDialog.value -> {
            AlertDialogExample(
                onDismissRequest = { openAlertDialog.value = false },
                onConfirmation = {
                    openAlertDialog.value = false
                  onConfirmation(inputState)
                },
                dialogTitle = "Enter password of tent to connect to it.",

                icon = Icons.Default.Info,
                inputState = inputState
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlertDialogExample(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    icon: ImageVector,
    inputState: TextFieldState
) {
    AlertDialog(
        icon = {
            Icon(icon, contentDescription = "Icon")
        },

        title = {
            Text(text = dialogTitle)
        },
        text = {
            BasicTextField2(state = inputState)
        },
        onDismissRequest = {
            onDismissRequest()
        },

        confirmButton = {
            TextButton (
                onClick = {
                    onConfirmation()
                }
            ) {
                Text("Confirm")
            }
        },


        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Dismiss")
            }
        }
    )
}