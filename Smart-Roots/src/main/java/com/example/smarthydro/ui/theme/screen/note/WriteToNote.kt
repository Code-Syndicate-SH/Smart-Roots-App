package com.example.smarthydro.ui.theme.screen.note

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.smarthydro.R
import com.example.smarthydro.ui.theme.AutoBlue
import com.example.smarthydro.ui.theme.DeepBlue
import com.example.smarthydro.ui.theme.SO_OnSurf_D
import com.example.smarthydro.ui.theme.SO_Surf_D
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.File

// ViewModel to handle Realtime Database operations
class NoteViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance().reference

    fun addNote(title: String, description: String, imageUri: Uri?, context: Context) {
        val noteId = database.child("notes").push().key
        if (noteId != null && imageUri != null) {
            viewModelScope.launch {
                try {
                    // Convert the image to a Base64 string
                    val imageBase64 = convertImageToBase64(imageUri, context)

                    // Get the current time in milliseconds
                    val timestamp = System.currentTimeMillis()

                    val note = mapOf(
                        "title" to title,
                        "description" to description,
                        "image" to imageBase64,
                        "timestamp" to timestamp  // Add this line
                    )

                    database.child("notes").child(noteId).setValue(note).await()
                    Log.d("NoteViewModel", "Note added successfully")
                } catch (e: Exception) {
                    Log.e("NoteViewModel", "Error adding note", e)
                }
            }
        }
    }


    private fun convertImageToBase64(imageUri: Uri, context: Context): String {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteToNote(padding: PaddingValues) {
    // --- ALL EXISTING LOGIC AND STATE IS PRESERVED ---
    val context = LocalContext.current
    val viewModel: NoteViewModel = viewModel()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }

    val leagueSpartan = FontFamily(Font(R.font.leaguespartan_semibold))

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    fun createImageFile(context: Context): Uri {
        val storageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "AutoGrow"
        )
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        val photoFile = File(storageDir, "photo_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", photoFile)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            Toast.makeText(context, "Photo captured successfully", Toast.LENGTH_SHORT).show()
            capturedImageUri?.let { uri ->
                imageUri = uri
            }
        } else {
            capturedImageUri = null
            Toast.makeText(context, "Failed to capture photo", Toast.LENGTH_SHORT).show()
        }
    }

    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // This logic is slightly different from your original, you may need to adjust
            // how capturedImageUri is set before launching the camera
            val newUri = createImageFile(context)
            capturedImageUri = newUri
            cameraLauncher.launch(newUri)
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }


    val themedTextFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = SO_Surf_D,
        unfocusedContainerColor = SO_Surf_D,
        disabledContainerColor = SO_Surf_D,
        cursorColor = AutoBlue,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        focusedTextColor = SO_OnSurf_D,
        unfocusedTextColor = SO_OnSurf_D,
        focusedLabelColor = SO_OnSurf_D.copy(alpha = 0.7f),
        unfocusedLabelColor = SO_OnSurf_D.copy(alpha = 0.7f),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = DeepBlue) // Themed background
            .padding(padding)
            .verticalScroll(rememberScrollState()), // Makes content scrollable
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SO_Surf_D)
                .border(BorderStroke(1.dp, AutoBlue.copy(alpha = 0.6f)), RoundedCornerShape(16.dp))
                .clickable() { galleryLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "Selected image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = "Add a photo placeholder",
                        tint = SO_OnSurf_D.copy(alpha = 0.7f),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        "Tap to select an image",
                        color = SO_OnSurf_D.copy(alpha = 0.7f),
                        fontFamily = leagueSpartan
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- Themed Action Buttons ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, AutoBlue.copy(alpha = 0.8f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SO_OnSurf_D)
            ) {
                Icon(Icons.Default.AddAPhoto, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Upload", fontFamily = leagueSpartan)
            }

            OutlinedButton (
                onClick = { requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, AutoBlue.copy(alpha = 0.8f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SO_OnSurf_D)
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Take Photo", fontFamily = leagueSpartan)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- Themed TextFields ---
        TextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            textStyle = TextStyle(fontFamily = leagueSpartan, fontSize = 18.sp),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = themedTextFieldColors,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            textStyle = TextStyle(fontFamily = leagueSpartan, fontSize = 16.sp),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(12.dp),
            colors = themedTextFieldColors
        )

        Spacer(modifier = Modifier.weight(1f)) // Pushes button to the bottom

        // --- Themed Save Button ---
        Button(
            onClick = {
                // --- ORIGINAL LOGIC IS PRESERVED ---
                if (title.isNotEmpty() && description.isNotEmpty() && imageUri != null) {
                    viewModel.addNote(title, description, imageUri, context)
                    title = ""
                    description = ""
                    imageUri = null
                    capturedImageUri = null
                } else {
                    Toast.makeText(context, "Please fill in all fields and add an image", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AutoBlue)
        ) {
            Text(
                "Save Note",
                style = TextStyle(fontFamily = leagueSpartan, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}