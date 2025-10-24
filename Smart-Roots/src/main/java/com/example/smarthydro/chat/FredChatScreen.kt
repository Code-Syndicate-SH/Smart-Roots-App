package com.example.smarthydro.chat

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.InputStream

// 🧩 Helper: safely decode Base64 to Bitmap for stored chat images
fun decodeBase64ToBitmap(base64: String): ImageBitmap? {
    return try {
        val cleanBase64 = base64.trim().replace("\n", "")
        val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}

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
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // 📸 Camera & Gallery launchers
    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap -> capturedBitmap = bitmap }

    val pickGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> selectedImageUri = uri }

    // 🪟 Choose image source
    if (showImagePickerDialog) {
        AlertDialog(
            onDismissRequest = { showImagePickerDialog = false },
            title = { Text("Select Image Source") },
            text = { Text("Choose whether to take a new photo or pick one from your gallery.") },
            confirmButton = {
                Button(onClick = {
                    takePhotoLauncher.launch(null)
                    showImagePickerDialog = false
                }) { Text("Take Photo") }
            },
            dismissButton = {
                Button(onClick = {
                    pickGalleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                    showImagePickerDialog = false
                }) { Text("Pick from Gallery") }
            }
        )
    }

    // 🧭 Auto-scroll to bottom when new messages or thinking bubble appear
    LaunchedEffect(messages.size, isThinking) {
        coroutineScope.launch { listState.animateScrollToItem(messages.size) }
    }

    Scaffold(containerColor = Color(0xFF0D0D0D)) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 💬 Chat history
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { msg ->
                    if (msg.content.startsWith("image://")) {
                        ChatImageBubble(msg.content.removePrefix("image://"))
                    } else {
                        ChatBubble(msg.content, isUser = msg.role == "user")
                    }
                }

                if (isThinking) {
                    item { ChatBubble("Thinking...", isUser = false) }
                }
            }

            // 🖼 Preview before sending
            if (capturedBitmap != null || selectedImageUri != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .background(Color(0xFF1B5E20), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        capturedBitmap != null -> {
                            Image(
                                bitmap = capturedBitmap!!.asImageBitmap(),
                                contentDescription = "Captured Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        selectedImageUri != null -> {
                            Image(
                                painter = rememberAsyncImagePainter(selectedImageUri),
                                contentDescription = "Selected Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            capturedBitmap = null
                            selectedImageUri = null
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(28.dp)
                            .background(Color(0xAA000000), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove Image",
                            tint = Color.White
                        )
                    }
                }
            }

            // 🧑‍🌾 Input area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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

                OutlinedTextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    placeholder = { Text("Type your message...", color = Color.Gray) },
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White, RoundedCornerShape(16.dp)),
                    textStyle = TextStyle(color = Color.Black)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        when {
                            capturedBitmap != null -> {
                                val stream = ByteArrayOutputStream()
                                capturedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 60, stream) // smaller
                                val bytes = stream.toByteArray()
                                val base64 = Base64.encodeToString(bytes, Base64.DEFAULT)
                                onSendImage(userInput.ifBlank { "Analyze this plant" }, bytes, "image/jpeg")
                                onSend("image://$base64")
                                capturedBitmap = null
                                userInput = ""
                            }

                            selectedImageUri != null -> {
                                val resolver = context.contentResolver
                                val inputStream: InputStream? = resolver.openInputStream(selectedImageUri!!)
                                val bytes = inputStream?.readBytes()
                                inputStream?.close()

                                if (bytes != null) {
                                    val base64 = Base64.encodeToString(bytes, Base64.DEFAULT)
                                    onSendImage(userInput.ifBlank { "Analyze this plant" }, bytes, "image/jpeg")
                                    onSend("image://$base64")
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
    }
}

// 🌿 Fred’s chat bubble with bullet + bold text
@Composable
fun ChatBubble(message: String, isUser: Boolean) {
    val bubbleColor = if (isUser) Color(0xFF43A047) else Color(0xFF1E1E1E)
    val textColor = if (isUser) Color.White else Color(0xFFEAEAEA)
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart

    val formattedText = remember(message) {
        buildAnnotatedString {
            val lines = message.split("\n")
            for (line in lines) {
                when {
                    line.trim().startsWith("* ") || line.trim().startsWith("- ") -> {
                        append("• ")
                        append(line.trim().removePrefix("* ").removePrefix("- "))
                    }
                    line.contains("**") -> {
                        var remaining = line
                        var bold = false
                        while (remaining.contains("**")) {
                            val start = remaining.indexOf("**")
                            val end = remaining.indexOf("**", start + 2)
                            if (end == -1) break
                            val before = remaining.substring(0, start)
                            val boldText = remaining.substring(start + 2, end)
                            append(before)
                            withStyle(style = androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(boldText)
                            }
                            remaining = remaining.substring(end + 2)
                            bold = true
                        }
                        if (!bold) append(remaining)
                    }
                    else -> append(line)
                }
                append("\n")
            }
        }
    }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Card(
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Text(
                text = formattedText,
                color = textColor,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

// 🌱 Display decoded Base64 image in chat
@Composable
fun ChatImageBubble(base64: String) {
    val bitmap = remember(base64) {
        try {
            val cleanBase64 = base64.trim().replace("\n", "")
            val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            modifier = Modifier
                .widthIn(max = 280.dp)
                .heightIn(min = 160.dp, max = 240.dp)
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = "Sent Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color(0xFF2E7D32), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Loading image...", color = Color.White, fontSize = 14.sp)
                }
            }
        }
    }
}
