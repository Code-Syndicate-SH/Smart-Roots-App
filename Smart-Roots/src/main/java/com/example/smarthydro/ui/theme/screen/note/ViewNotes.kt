package com.example.smarthydro.ui.theme.screen.note

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.database.*
import com.itextpdf.text.Document
import com.itextpdf.text.DocumentException
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.coroutines.launch
import leagueSpartan
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.IOException

data class Note(
    val title: String = "",
    val timestamp: Long = 0L,
    val description: String = "",
    val image: String = ""
)

class ViewNotesViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance().reference.child("notes")
    var notes by mutableStateOf<List<Note>>(emptyList())
        private set
    var isLoading by mutableStateOf(true)

    init { fetchNotes() }

    private fun fetchNotes() {
        viewModelScope.launch {
            database.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val noteList = mutableListOf<Note>()
                    for (noteSnapshot in snapshot.children) {
                        val note = noteSnapshot.getValue(Note::class.java)
                        note?.let { noteList.add(it) }
                    }
                    notes = noteList
                    isLoading = false
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("ViewNotesViewModel", "Error fetching notes", error.toException())
                    isLoading = false
                }
            })
        }
    }
}

fun createPdfFromNotes(context: Context, notes: List<Note>) {
    val document = Document()
    val filePath = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/notes.pdf"
    try {
        PdfWriter.getInstance(document, FileOutputStream(filePath))
        document.open()
        for (note in notes) {
            document.add(Paragraph("Title: ${note.title}"))
            document.add(Paragraph("Date: ${formatTimestamp(note.timestamp)}"))
            document.add(Paragraph("Description: ${note.description}"))
            note.image.takeIf { it.isNotEmpty() }?.let {
                val bitmap = decodeBase64Image(it)
                bitmap?.let { bmp ->
                    val stream = ByteArrayOutputStream()
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    val img = com.itextpdf.text.Image.getInstance(stream.toByteArray())
                    document.add(img)
                }
            }
            document.add(Paragraph("\n"))
        }
        document.close()
        Toast.makeText(context, "PDF created successfully!", Toast.LENGTH_SHORT).show()
    } catch (e: DocumentException) { e.printStackTrace()
    } catch (e: IOException) { e.printStackTrace() }
}

@Composable
fun ViewNotes() {
    val cs = MaterialTheme.colorScheme
    val vm: ViewNotesViewModel = viewModel()
    // Reading state directly is fine; it's a mutableState in the VM
    val notes = vm.notes
    val isLoading = vm.isLoading

    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val density = LocalDensity.current.density
    val itemWidthPx = 320.dp.toPx(density)
    val maxOffsetPx = 30.dp.toPx(density)

    // ✅ FIX: wrap derivedStateOf in remember
    val snapIndex by remember(scrollState, itemWidthPx) {
        derivedStateOf {
            val infos = scrollState.layoutInfo.visibleItemsInfo
            infos.minByOrNull { itemInfo ->
                val centerX = scrollState.layoutInfo.viewportSize.width / 2
                val itemCenterX = itemInfo.offset + (itemWidthPx / 2)
                kotlin.math.abs(itemCenterX - centerX)
            }?.index ?: 0
        }
    }

    LaunchedEffect(snapIndex) {
        coroutineScope.launch { scrollState.animateScrollToItem(snapIndex) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.background)
            .padding(16.dp)
    ) {
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Loading...",
                    color = cs.primary,
                    style = TextStyle(fontSize = 48.sp),
                    fontFamily = leagueSpartan,
                    fontWeight = FontWeight.Bold,
                )
            }
        } else if (notes.isNotEmpty()) {
            LazyRow(
                state = scrollState,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(notes) { index, note ->
                    val viewportWidthPx = scrollState.layoutInfo.viewportSize.width.toFloat()
                    val itemOffsetPx = scrollState.layoutInfo.visibleItemsInfo
                        .find { it.index == index }?.offset ?: 0
                    val itemCenterPx = itemOffsetPx + (itemWidthPx / 2)
                    val centerX = viewportWidthPx / 2
                    val distanceFromCenter = itemCenterPx - centerX

                    val maxScale = 1.08f
                    val minScale = 0.92f
                    val scale = maxScale - (kotlin.math.abs(distanceFromCenter) / (viewportWidthPx / 2)) * (maxScale - minScale)
                    val offsetX = (distanceFromCenter / viewportWidthPx) * maxOffsetPx

                    NoteCard(
                        note = note,
                        onDownloadClick = { createPdfFromNotes(context, listOf(note)) },
                        modifier = Modifier
                            .graphicsLayer(scaleX = scale, scaleY = scale)
                            .width(320.dp)
                            .height(700.dp)
                            .offset(x = offsetX.toDp(density))
                    )
                }
            }
        } else {
            Text(
                "No notes available",
                color = cs.onBackground,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

// ---- Utility converters: NOT composable ----
private fun Float.toDp(density: Float): Dp = (this / density).dp
private fun Dp.toPx(density: Float): Float = (this.value * density)

@Composable
fun NoteCard(note: Note, onDownloadClick: () -> Unit, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme

    // Use Surface's elevation; avoid graphicsLayer().toPx() to keep density out
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = cs.surface,
        tonalElevation = 1.dp,
        shadowElevation = 20.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(Base64.decode(note.image, Base64.DEFAULT))
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = note.title,
                style = TextStyle(
                    fontFamily = leagueSpartan,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = cs.onSurface
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = formatTimestamp(note.timestamp),
                style = TextStyle(
                    fontFamily = leagueSpartan,
                    fontSize = 14.sp,
                    color = cs.onSurface.copy(alpha = 0.7f)
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = note.description,
                        style = TextStyle(
                            fontFamily = leagueSpartan,
                            fontSize = 16.sp,
                            color = cs.onSurface.copy(alpha = 0.8f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }
            }

            Button(
                onClick = onDownloadClick,
                colors = ButtonDefaults.buttonColors(containerColor = cs.primary, contentColor = cs.onPrimary),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp, bottom = 4.dp)
            ) {
                Icon(Icons.Filled.Info, contentDescription = "Info")
                Spacer(Modifier.width(8.dp))
                Text("Download PDF")
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}

private val bitmapCache = mutableMapOf<String, Bitmap?>()

private fun decodeBase64Image(base64String: String): Bitmap? =
    bitmapCache[base64String] ?: try {
        val decoded = Base64.decode(base64String, Base64.DEFAULT)
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            BitmapFactory.decodeByteArray(decoded, 0, decoded.size, this)
            val reqWidth = 200
            val reqHeight = 200
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
            inJustDecodeBounds = false
        }
        BitmapFactory.decodeByteArray(decoded, 0, decoded.size, options).also {
            bitmapCache[base64String] = it
        }
    } catch (e: IllegalArgumentException) {
        Log.e("NoteCard", "Invalid Base64 string", e); null
    }

private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val (width, height) = options.outWidth to options.outHeight
    var inSampleSize = 1
    if (height > reqHeight || width > reqWidth) {
        var halfHeight = height / 2
        var halfWidth = width / 2
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
            halfHeight /= 2
            halfWidth /= 2
        }
    }
    return inSampleSize
}