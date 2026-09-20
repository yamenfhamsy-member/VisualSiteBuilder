// Draggable design canvas rendering a list of elements.
package com.example.visualsitebuilder.ui

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.visualsitebuilder.model.DesignElement
import com.example.visualsitebuilder.model.ElementType
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun DesignCanvas(
    elements: List<DesignElement>,
    selectedId: String?,
    onElementMoved: (id: String, dx: Float, dy: Float) -> Unit,
    onElementResized: (id: String, dx: Float, dy: Float) -> Unit,
    onElementSelected: (id: String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        elements.forEach { element ->
            val isSelected = element.id == selectedId
            Box(
                modifier = Modifier
                    .offset { IntOffset(element.x.roundToInt(), element.y.roundToInt()) }
                    .size(element.width.dp, element.height.dp)
                    .background(parseColorSafe(element.backgroundColor))
                    .then(
                        if (isSelected) {
                            Modifier.border(2.dp, Color.Blue)
                        } else {
                            Modifier
                        }
                    )
                    .pointerInput(element.id) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onElementMoved(element.id, dragAmount.x, dragAmount.y)
                        }
                    }
                    .clickable { onElementSelected(element.id) }
            ) {
                when (element.type) {
                    ElementType.TEXT -> {
                        Text(
                            text = element.text,
                            color = parseColorSafe(element.textColor),
                            fontSize = element.fontSize.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    ElementType.HEADING_1,
                    ElementType.HEADING_2,
                    ElementType.HEADING_3,
                    ElementType.HEADING_4,
                    ElementType.HEADING_5,
                    ElementType.HEADING_6 -> {
                        Text(
                            text = element.text,
                            color = parseColorSafe(element.textColor),
                            fontSize = element.fontSize.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    ElementType.LINK -> {
                        Text(
                            text = element.text,
                            color = Color(0xFF1A73E8),
                            fontSize = element.fontSize.sp,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    ElementType.LABEL -> {
                        Text(
                            text = element.text,
                            color = parseColorSafe(element.textColor),
                            fontSize = element.fontSize.sp,
                            modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp)
                        )
                    }
                    ElementType.DIVIDER -> {
                        HorizontalDivider(
                            thickness = 2.dp,
                            color = parseColorSafe(element.textColor),
                            modifier = Modifier.align(Alignment.Center).padding(horizontal = 4.dp)
                        )
                    }
                    ElementType.BUTTON -> {
                        Button(
                            onClick = { onElementSelected(element.id) },
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Text(text = element.text)
                        }
                    }
                    ElementType.INPUT -> {
                        OutlinedTextField(
                            value = element.text,
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            placeholder = { Text(text = element.hint) },
                            singleLine = true,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    ElementType.TEXTAREA -> {
                        OutlinedTextField(
                            value = element.text,
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            placeholder = { Text(text = element.hint) },
                            minLines = 2,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    ElementType.IMAGE -> {
                        if (element.imageUri.isBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.LightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = element.text.ifEmpty { "Image" })
                            }
                        } else {
                            GalleryImage(uriString = element.imageUri)
                        }
                    }
                    ElementType.VIDEO -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF222222)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "▶ Video", color = Color.White)
                        }
                    }
                    ElementType.LIST, ElementType.ORDERED_LIST -> {
                        val items = listOf(element.text) +
                            element.children.filter { it.type == ElementType.LIST_ITEM }.map { it.text }
                        Column(modifier = Modifier.align(Alignment.TopStart).padding(6.dp)) {
                            items.forEachIndexed { index, item ->
                                val marker = if (element.type == ElementType.LIST) "•" else "${index + 1}."
                                Text(
                                    text = "$marker $item",
                                    color = parseColorSafe(element.textColor),
                                    fontSize = element.fontSize.sp
                                )
                            }
                        }
                    }
                    ElementType.LIST_ITEM -> {
                        Text(
                            text = "• ${element.text}",
                            color = parseColorSafe(element.textColor),
                            fontSize = element.fontSize.sp,
                            modifier = Modifier.align(Alignment.CenterStart).padding(start = 6.dp)
                        )
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(1.dp, Color.Gray),
                            contentAlignment = Alignment.Center
                        ) {
                            val count = element.children.size
                            val suffix = if (count > 0) " · $count" else ""
                            Text(
                                text = element.type.htmlTag + suffix,
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(24.dp)
                            .background(Color.Blue)
                            .pointerInput(element.id) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    onElementResized(element.id, dragAmount.x, dragAmount.y)
                                }
                            }
                    )
                }
            }
        }
    }
}

private fun parseColorSafe(value: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(value))
    } catch (e: IllegalArgumentException) {
        Color.White
    }
}

@Composable
private fun GalleryImage(uriString: String) {
    val context = LocalContext.current
    var bitmap by remember(uriString) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(uriString) {
        bitmap = withContext(Dispatchers.IO) {
            decodeSampled(context.contentResolver, Uri.parse(uriString))
        }
    }
    val bmp = bitmap
    if (bmp != null) {
        Image(
            bitmap = bmp.asImageBitmap(),
            contentDescription = "Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Image")
        }
    }
}

private fun decodeSampled(resolver: ContentResolver, uri: Uri, maxDim: Int = 1024): Bitmap? {
    return try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sample > maxDim) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
    } catch (e: Exception) {
        null
    }
}
