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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.smarthydro.R
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.File

// ---------- ViewModel (kept as in your file) ----------
class NoteViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance().reference

    fun addNote(title: String, description: String, imageUri: Uri?, context: Context) {
        val noteId = database.child("notes").push().key
        if (noteId != null && imageUri != null) {
            viewModelScope.launch {
                try {
                    val imageBase64 = convertImageToBase64(imageUri, context)
                    val timestamp = System.currentTimeMillis()
                    val note = mapOf(
                        "title" to title,
                        "description" to description,
                        "image" to imageBase64,
                        "timestamp" to timestamp
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
        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
        val byteArray = output.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}

// ---------- UI (same structure; themed; fixed smart-cast) ----------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteToNote() {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    val viewModel: NoteViewModel = viewModel()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // Keep this as state, but never pass it directly to launch()
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }

    val leagueSpartan = FontFamily(Font(R.font.leaguespartan_semibold))

    // Gallery picker (unchanged)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }

    // Camera capture (TakePicture expects a non-null Uri)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            Toast.makeText(context, "Photo captured", Toast.LENGTH_SHORT).show()
            // No smart cast: just use the state value safely
            imageUri = capturedImageUri
        } else {
            capturedImageUri = null
            Toast.makeText(context, "Failed to capture photo", Toast.LENGTH_SHORT).show()
        }
    }

    // Permission request (FIX: create a local val for the Uri and pass that to launch)
    val requestCameraPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri: Uri = createImageFile(context) // local non-null value
            capturedImageUri = uri                  // keep in state for later use
            cameraLauncher.launch(uri)              // pass the local val (prevents smart-cast error)
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header chip (Notes • Open)
        Surface(
            color = cs.surface,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = cs.secondary,
                    shape = RoundedCornerShape(100),
                    modifier = Modifier
                        .width(36.dp)
                        .height(36.dp)
                ) {}
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Notes", color = cs.onSurface.copy(alpha = 0.7f))
                    Text("Open", color = cs.onSurface, style = MaterialTheme.typography.headlineSmall)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Card like your video
        Surface(
            color = cs.surface,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {

                Text(
                    "Write a note",
                    color = cs.onSurface,
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = leagueSpartan)
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title", fontFamily = leagueSpartan) },
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = cs.primary,
                        unfocusedBorderColor = cs.secondary,
                        cursorColor = cs.primary,
                        focusedLabelColor = cs.onSurface.copy(alpha = 0.8f),
                        unfocusedLabelColor = cs.onSurface.copy(alpha = 0.7f),
                        focusedTextColor = cs.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Body", fontFamily = leagueSpartan) },
                    minLines = 4,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = cs.primary,
                        unfocusedBorderColor = cs.secondary,
                        cursorColor = cs.primary,
                        focusedLabelColor = cs.onSurface.copy(alpha = 0.8f),
                        unfocusedLabelColor = cs.onSurface.copy(alpha = 0.7f),
                        focusedTextColor = cs.onSurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )

                if (imageUri != null) {
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        color = cs.surface,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { requestCameraPermission.launch(Manifest.permission.CAMERA) },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = cs.primary,
                            contentColor = cs.onPrimary
                        )
                    ) {
                        Text("Take picture", fontFamily = leagueSpartan)
                    }

                    Spacer(Modifier.width(12.dp))

                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = cs.onSurface
                        )
                    ) {
                        Text("Add picture", fontFamily = leagueSpartan)
                    }

                    Spacer(Modifier.weight(1f))

                    Button(
                        onClick = {
                            if (title.isNotEmpty() && description.isNotEmpty() && imageUri != null) {
                                viewModel.addNote(title, description, imageUri, context)
                                title = ""; description = ""; imageUri = null; capturedImageUri = null
                                Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Please complete all fields", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = cs.primary,
                            contentColor = cs.onPrimary
                        )
                    ) {
                        Text("Save", fontFamily = leagueSpartan)
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

// ---------- Helper (same path/signature as yours) ----------
private fun createImageFile(context: Context): Uri {
    val storageDir = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        "AutoGrow"
    )
    if (!storageDir.exists()) storageDir.mkdirs()
    val photoFile = File(storageDir, "photo_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", photoFile)
}