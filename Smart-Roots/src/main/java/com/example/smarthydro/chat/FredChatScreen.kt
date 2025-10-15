package com.example.smarthydro.chat

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import java.io.ByteArrayOutputStream
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FredChatScreen(
    messages: List<UiMsg>,
    isThinking: Boolean,
    onSend: (String) -> Unit,
    onSendImage: (String, ByteArray, String) -> Unit
) {
    var userInput by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showImagePickerDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // 📷 Take a photo launcher
    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { bitmap -> capturedBitmap = bitmap }
    )

    // 🖼️ Pick from gallery launcher
    val pickGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            selectedImageUri = uri
        }
    )

    // 🟢 Image selection dialog
    if (showImagePickerDialog) {
        AlertDialog(
            onDismissRequest = { showImagePickerDialog = false },
            title = { Text("Select Image Source") },
            text = { Text("Choose whether to take a new photo or pick one from your gallery.") },
            confirmButton = {
                Button(onClick = {
                    takePhotoLauncher.launch(null)
                    showImagePickerDialog = false
                }) {
                    Text("Take Photo")
                }
            },
            dismissButton = {
                Button(onClick = {
                    pickGalleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    showImagePickerDialog = false
                }) {
                    Text("Pick from Gallery")
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D)),
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 🧩 Image selection button
                IconButton(
                    onClick = { showImagePickerDialog = true },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF2E7D32), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Attach or Take Photo",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 💬 Text field
                OutlinedTextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    placeholder = { Text("Type your message...", color = Color.Gray) },
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF1B1B1B), RoundedCornerShape(16.dp)),
                    textStyle = TextStyle(color = Color.White)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // 📨 Send button
                IconButton(
                    onClick = {
                        when {
                            capturedBitmap != null -> {
                                val stream = ByteArrayOutputStream()
                                capturedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                                val bytes = stream.toByteArray()
                                onSendImage(userInput.ifBlank { "Analyze this plant" }, bytes, "image/jpeg")
                                capturedBitmap = null
                                userInput = ""
                            }
                            selectedImageUri != null -> {
                                val resolver = context.contentResolver
                                val inputStream: InputStream? =
                                    resolver.openInputStream(selectedImageUri!!)
                                val bytes = inputStream?.readBytes()
                                inputStream?.close()

                                if (bytes != null) {
                                    onSendImage(userInput.ifBlank { "Analyze this plant" }, bytes, "image/jpeg")
                                }
                                selectedImageUri = null
                                userInput = ""
                            }
                            userInput.isNotBlank() -> {
                                onSend(userInput)
                                userInput = ""
                            }
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF43A047), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Message",
                        tint = Color.White
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(
                    message = msg.content,
                    isUser = msg.role == "user"
                )
            }

            if (isThinking) {
                item {
                    ChatBubble(message = "Thinking...", isUser = false)
                }
            }
        }

        // 🖼️ Show image preview
        when {
            capturedBitmap != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(8.dp)
                        .background(Color(0xFF1B5E20), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = capturedBitmap!!.asImageBitmap(),
                        contentDescription = "Captured Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            selectedImageUri != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(8.dp)
                        .background(Color(0xFF1B5E20), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(selectedImageUri),
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: String, isUser: Boolean) {
    val bubbleColor = if (isUser) Color(0xFF43A047) else Color(0xFF1E1E1E)
    val textColor = if (isUser) Color.White else Color.LightGray
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                text = message,
                color = textColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}
