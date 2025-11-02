package com.example.smarthydro.ui.theme.screen.note

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smarthydro.ui.theme.AutoBlue
import com.example.smarthydro.ui.theme.DeepBlue
import com.example.smarthydro.ui.theme.SO_OnSurf_D
import com.example.smarthydro.ui.theme.SO_Surf_D
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.itextpdf.text.Document
import com.itextpdf.text.DocumentException
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import leagueSpartan
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream

data class Note(
    val title: String = "",
    val timestamp: Long = 0L,
    val description: String = "",
    val image: String = "",
    var decodedBitmap: Bitmap? = null
)


class ViewNotesViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance().reference.child("notes")
    var notes by mutableStateOf<List<Note>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)

    init {
        fetchNotes()
    }

    private fun fetchNotes() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val noteList = mutableListOf<Note>()
                for (noteSnapshot in snapshot.children) {
                    val note = noteSnapshot.getValue(Note::class.java)
                    note?.let { noteList.add(it) }
                }

                // OPTIMIZATION 2: Pre-decode all bitmaps on a background thread
                viewModelScope.launch(Dispatchers.Default) {
                    noteList.forEach { note ->
                        if (note.image.isNotEmpty()) {
                            // Use our optimized decoding function
                            note.decodedBitmap = decodeBase64Image(note.image)
                        }
                    }
                    // Once decoding is done, update the state on the main thread
                    launch(Dispatchers.Main) {
                        notes = noteList
                        isLoading = false
                    }
                }
                Log.d("ViewNotesViewModel", "Notes fetched: ${noteList.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewNotesViewModel", "Error fetching notes", error.toException())
                isLoading = false
            }
        })
    }

    // OPTIMIZATION 3: Clear the cache when the ViewModel is no longer used
    override fun onCleared() {
        super.onCleared()
        bitmapCache.clear()
    }
}

fun createPdfFromNotes(context: Context, notes: List<Note>) {
    val document = Document()
    val filePath = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/notes_report.pdf"

    try {
        PdfWriter.getInstance(document, FileOutputStream(filePath))
        document.open()

        for (note in notes) {
            document.add(Paragraph("Title: ${note.title}"))
            document.add(Paragraph("Date: ${formatTimestamp(note.timestamp)}"))
            document.add(Paragraph("Description: ${note.description}"))

            // Use the pre-decoded bitmap if available, otherwise decode it now
            val bitmapToUse = note.decodedBitmap ?: decodeBase64Image(note.image)

            bitmapToUse?.let { bmp ->
                val imageStream = ByteArrayOutputStream()
                bmp.compress(Bitmap.CompressFormat.PNG, 100, imageStream)
                val imageBytes = imageStream.toByteArray()
                val image = com.itextpdf.text.Image.getInstance(imageBytes)
                image.scaleToFit(500f, 500f)
                document.add(image)
            }
            document.add(Paragraph("\n"))
        }

        document.close()
        Toast.makeText(context, "PDF saved to Downloads folder!", Toast.LENGTH_LONG).show()
    } catch (e: Exception) { // Catch generic exception for robustness
        e.printStackTrace()
        Toast.makeText(context, "Error creating PDF: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}


@Composable
fun ViewNotes(paddingValues: PaddingValues) {
    val viewModel: ViewNotesViewModel = viewModel()
    val notes by remember { derivedStateOf { viewModel.notes } }
    val isLoading by remember { derivedStateOf { viewModel.isLoading } }

    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    val density = LocalDensity.current.density
    val itemWidthPx = 320.dp.toPx(density)
    val maxOffsetPx = 30.dp.toPx(density)

    val snapIndex by derivedStateOf {
        val visibleItemInfos = scrollState.layoutInfo.visibleItemsInfo
        if (visibleItemInfos.isEmpty()) {
            0
        } else {
            visibleItemInfos.minByOrNull { itemInfo ->
                val centerX = scrollState.layoutInfo.viewportSize.width / 2
                val itemCenterX = itemInfo.offset + (itemWidthPx / 2)
                Math.abs(itemCenterX - centerX)
            }?.index ?: 0
        }
    }

    LaunchedEffect(snapIndex) {
        if (notes.isNotEmpty()) {
            coroutineScope.launch {
                scrollState.animateScrollToItem(snapIndex)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = DeepBlue)
            .padding(paddingValues)
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Loading...",
                    color = AutoBlue,
                    style = TextStyle(fontSize = 50.sp),
                    fontFamily = leagueSpartan,
                    fontWeight = FontWeight.Bold,
                )
            }
        } else if (notes.isNotEmpty()) {
            LazyRow(
                state = scrollState,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 32.dp)
            ) {
                itemsIndexed(notes, key = { _, note -> note.timestamp }) { index, note ->
                    val viewportWidthPx = scrollState.layoutInfo.viewportSize.width.toFloat()
                    val itemOffsetPx = scrollState.layoutInfo.visibleItemsInfo
                        .find { it.index == index }?.offset ?: 0
                    val itemCenterPx = itemOffsetPx + (itemWidthPx / 2)
                    val centerX = viewportWidthPx / 2
                    val distanceFromCenter = itemCenterPx - centerX

                    val maxScale = 1.05f
                    val minScale = 0.85f
                    val scaleRange = maxScale - minScale
                    val scale = maxScale - (Math.abs(distanceFromCenter) / (viewportWidthPx / 2)) * scaleRange

                    val offsetX = (distanceFromCenter / viewportWidthPx) * maxOffsetPx

                    // OPTIMIZATION 4: Stabilize the lambda by remembering it.
                    val onDownloadClick = remember(context, note) {
                        { createPdfFromNotes(context, listOf(note)) }
                    }

                    NoteCard(
                        note = note,
                        onDownloadClick = onDownloadClick,
                        modifier = Modifier
                            .graphicsLayer(
                                scaleX = scale.toFloat(),
                                scaleY = scale.toFloat()
                            )
                            .width(320.dp)
                            .height(650.dp)
                            .offset(x = offsetX.toDp(density))
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No notes available",
                    color = SO_OnSurf_D,
                    style = TextStyle(fontSize = 24.sp, fontFamily = leagueSpartan),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}


@Composable
fun Float.toDp(density: Float): Dp {
    return (this / density).dp
}


@Composable
fun NoteCard(note: Note, onDownloadClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = SO_Surf_D,
        border = BorderStroke(1.dp, AutoBlue.copy(alpha = 0.6f)),
        modifier = modifier
            .padding(1.dp)
            .graphicsLayer {
                shadowElevation = 8.dp.toPx()
            },
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // OPTIMIZATION 5: Use the pre-decoded bitmap. This is MUCH faster.
            val bitmap = note.decodedBitmap
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Note Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.Gray, shape = RoundedCornerShape(12.dp))
                )
            } else {
                // Fallback Box if there is no image or it failed to decode
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.Gray, shape = RoundedCornerShape(12.dp)))
            }


            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = note.title,
                style = TextStyle(
                    fontFamily = leagueSpartan,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = SO_OnSurf_D
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = formatTimestamp(note.timestamp),
                style = TextStyle(
                    fontFamily = leagueSpartan,
                    fontSize = 14.sp,
                    color = SO_OnSurf_D.copy(alpha = 0.7f)
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 8.dp)
            ) {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = note.description,
                        style = TextStyle(
                            fontFamily = leagueSpartan,
                            fontSize = 16.sp,
                            color = SO_OnSurf_D.copy(alpha = 0.85f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Button(
                onClick = { onDownloadClick() },
                colors = ButtonDefaults.buttonColors(containerColor = AutoBlue),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Download,
                    contentDescription = "Download",
                    modifier = Modifier.size(24.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Download PDF",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


private fun formatTimestamp(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    val date = java.util.Date(timestamp)
    return sdf.format(date)
}

@Composable
fun Dp.toPx(density: Float): Float {
    return this.value * density
}

private val bitmapCache = mutableMapOf<String, Bitmap?>()

private fun decodeBase64Image(base64String: String): Bitmap? {
    if (base64String.isEmpty()) return null
    // Use the string's hashcode as a key for caching, it's more efficient than the whole string
    val key = base64String.hashCode().toString()
    if (bitmapCache.containsKey(key)) {
        return bitmapCache[key]
    }

    return try {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size, this)
            inSampleSize = calculateInSampleSize(this, 512, 512)
            inJustDecodeBounds = false
        }
        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size, options)
        bitmapCache[key] = bitmap
        bitmap
    } catch (e: IllegalArgumentException) {
        Log.e("NoteCard", "Invalid Base64 string for image decoding", e)
        null
    }
}


private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val (height: Int, width: Int) = options.outHeight to options.outWidth
    var inSampleSize = 1
    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}