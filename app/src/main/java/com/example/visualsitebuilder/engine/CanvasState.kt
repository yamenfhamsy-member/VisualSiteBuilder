// Canvas state holder (ViewModel) exposing immutable StateFlow.
package com.example.visualsitebuilder.engine

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.visualsitebuilder.codegen.CssGenerator
import com.example.visualsitebuilder.codegen.HtmlGenerator
import com.example.visualsitebuilder.export.ZipExporter
import com.example.visualsitebuilder.model.DesignElement
import com.example.visualsitebuilder.model.ElementType
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CanvasState : ViewModel() {
    private val _elements = MutableStateFlow(
        listOf(
            DesignElement(
                id = "text1",
                type = ElementType.TEXT,
                x = 40f,
                y = 60f,
                width = 220f,
                height = 60f,
                text = "Hello Site",
                backgroundColor = "#FFFFFF",
                textColor = "#000000",
                fontSize = 18
            ),
            DesignElement(
                id = "btn1",
                type = ElementType.BUTTON,
                x = 40f,
                y = 160f,
                width = 180f,
                height = 56f,
                text = "Click Me",
                backgroundColor = "#2196F3",
                textColor = "#FFFFFF",
                fontSize = 16
            ),
            DesignElement(
                id = "box1",
                type = ElementType.CONTAINER,
                x = 40f,
                y = 260f,
                width = 260f,
                height = 140f,
                backgroundColor = "#EEEEEE"
            )
        )
    )
    val elements: StateFlow<List<DesignElement>> = _elements.asStateFlow()

    private val _selectedId = MutableStateFlow<String?>(null)
    val selectedId: StateFlow<String?> = _selectedId.asStateFlow()

    fun moveElement(id: String, dx: Float, dy: Float) {
        pushHistoryCoalesced(id)
        _elements.value = _elements.value.map { el ->
            if (el.id == id) {
                val (nx, ny) = DragDropHandler.applyDrag(el.x, el.y, dx, dy)
                el.copy(x = nx, y = ny)
            } else {
                el
            }
        }
    }

    fun resizeElement(id: String, dx: Float, dy: Float) {
        pushHistoryCoalesced(id)
        _elements.value = _elements.value.map { el ->
            if (el.id == id) {
                val (nw, nh) = DragDropHandler.applyResize(el.width, el.height, dx, dy)
                el.copy(width = nw, height = nh)
            } else {
                el
            }
        }
    }

    fun selectElement(id: String?) {
        _selectedId.value = id
    }

    private val undoStack = ArrayDeque<List<DesignElement>>()
    private val redoStack = ArrayDeque<List<DesignElement>>()
    private var lastHistoryId: String? = null
    private var lastHistoryTime = 0L

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    private fun DesignElement.deepCopy(): DesignElement =
        copy(children = children.map { it.deepCopy() }.toMutableList())

    private fun snapshot(): List<DesignElement> = _elements.value.map { it.deepCopy() }

    private fun refreshHistoryFlags() {
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
    }

    private fun pushHistory() {
        undoStack.addLast(snapshot())
        if (undoStack.size > 50) undoStack.removeFirst()
        redoStack.clear()
        lastHistoryId = null
        refreshHistoryFlags()
    }

    private fun pushHistoryCoalesced(id: String) {
        val now = System.currentTimeMillis()
        if (lastHistoryId != id || now - lastHistoryTime > 1500L) {
            undoStack.addLast(snapshot())
            if (undoStack.size > 50) undoStack.removeFirst()
            redoStack.clear()
            lastHistoryId = id
            refreshHistoryFlags()
        }
        lastHistoryTime = now
    }

    private fun clearHistory() {
        undoStack.clear()
        redoStack.clear()
        lastHistoryId = null
        refreshHistoryFlags()
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        redoStack.addLast(snapshot())
        if (redoStack.size > 50) redoStack.removeFirst()
        _elements.value = undoStack.removeLast()
        lastHistoryId = null
        if (_selectedId.value != null && _elements.value.none { it.id == _selectedId.value }) {
            _selectedId.value = null
        }
        refreshHistoryFlags()
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        undoStack.addLast(snapshot())
        if (undoStack.size > 50) undoStack.removeFirst()
        _elements.value = redoStack.removeLast()
        lastHistoryId = null
        if (_selectedId.value != null && _elements.value.none { it.id == _selectedId.value }) {
            _selectedId.value = null
        }
        refreshHistoryFlags()
    }

    fun updateElement(updated: DesignElement) {
        pushHistoryCoalesced(updated.id)
        _elements.value = _elements.value.map { el ->
            if (el.id == updated.id) updated else el
        }
    }

    private val _exportStatus = MutableStateFlow<String?>(null)
    val exportStatus: StateFlow<String?> = _exportStatus.asStateFlow()

    fun exportSite(resolver: ContentResolver, uri: Uri) {
        viewModelScope.launch {
            try {
                val elements = _elements.value
                val files = withContext(Dispatchers.IO) {
                    loadImageFiles(resolver, elements)
                }
                val ext = files.mapValues { it.value.second }
                val html = HtmlGenerator.generate(elements, ext)
                val css = CssGenerator.generate(elements)
                val images = files.mapValues { it.value.first }
                    .mapKeys { (id, _) -> "$id.${ext[id] ?: "png"}" }
                resolver.openOutputStream(uri)?.use { out ->
                    ZipExporter.exportToStream(out, html, css, images)
                } ?: throw IOException("Cannot open output")
                _exportStatus.value = "Exported OK"
            } catch (e: Exception) {
                _exportStatus.value = "Export failed: ${e.message}"
            }
        }
    }

    private val _previewHtml = MutableStateFlow<String?>(null)
    val previewHtml: StateFlow<String?> = _previewHtml.asStateFlow()

    fun requestPreview(resolver: ContentResolver) {
        viewModelScope.launch {
            try {
                val elements = _elements.value
                val dataUris = withContext(Dispatchers.IO) {
                    loadPreviewDataUris(resolver, elements)
                }
                _previewHtml.value = HtmlGenerator.generateStandalone(elements, dataUris)
            } catch (e: Exception) {
                _exportStatus.value = "Preview failed: ${e.message}"
            }
        }
    }

    fun clearPreview() {
        _previewHtml.value = null
    }

    private fun collectImages(elements: List<DesignElement>): List<DesignElement> =
        elements.flatMap { listOf(it) + collectImages(it.children) }
            .filter { it.type == ElementType.IMAGE && it.imageUri.isNotBlank() }

    private fun mimeToExt(mime: String?): String = when (mime) {
        "image/jpeg" -> "jpg"
        "image/png" -> "png"
        "image/webp" -> "webp"
        "image/gif" -> "gif"
        else -> "png"
    }

    private fun loadImageFiles(
        resolver: ContentResolver,
        elements: List<DesignElement>
    ): Map<String, Pair<ByteArray, String>> {
        val result = mutableMapOf<String, Pair<ByteArray, String>>()
        collectImages(elements).forEach { el ->
            try {
                val uri = Uri.parse(el.imageUri)
                val mime = resolver.getType(uri) ?: "image/png"
                resolver.openInputStream(uri)?.use { input ->
                    result[el.id] = input.readBytes() to mimeToExt(mime)
                }
            } catch (e: Exception) {
                // Skip unreadable images, export continues without them.
            }
        }
        return result
    }

    private fun loadPreviewDataUris(
        resolver: ContentResolver,
        elements: List<DesignElement>
    ): Map<String, String> {
        val result = mutableMapOf<String, String>()
        collectImages(elements).forEach { el ->
            try {
                val uri = Uri.parse(el.imageUri)
                val bitmap = decodeSampled(resolver, uri) ?: return@forEach
                val out = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                val base64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
                result[el.id] = "data:image/jpeg;base64,$base64"
            } catch (e: Exception) {
                // Skip unreadable images, preview continues without them.
            }
        }
        return result
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

    fun clearExportStatus() {
        _exportStatus.value = null
    }

    fun addElement(type: ElementType) {
        val id = "${type.name.lowercase()}_${UUID.randomUUID().toString().take(4)}"
        val base = _elements.value.size
        val element = when (type) {
            ElementType.TEXT -> DesignElement(
                id = id, type = type, x = 40f, y = 60f + base * 80,
                width = 220f, height = 60f, text = "New Text"
            )
            ElementType.BUTTON -> DesignElement(
                id = id, type = type, x = 40f, y = 60f + base * 80,
                width = 180f, height = 56f, text = "Button",
                backgroundColor = "#2196F3", textColor = "#FFFFFF"
            )
            ElementType.IMAGE -> DesignElement(
                id = id, type = type, x = 40f, y = 60f + base * 80,
                width = 160f, height = 120f, backgroundColor = "#CCCCCC", text = "Image"
            )
            ElementType.CONTAINER -> DesignElement(
                id = id, type = type, x = 40f, y = 60f + base * 80,
                width = 260f, height = 140f, backgroundColor = "#EEEEEE"
            )
        }
        pushHistory()
        _elements.value = _elements.value + element
        _selectedId.value = id
    }

    fun deleteSelected() {
        val id = _selectedId.value ?: return
        pushHistory()
        _elements.value = _elements.value.filter { it.id != id }
        _selectedId.value = null
    }

    fun duplicateSelected() {
        val selectedId = _selectedId.value ?: return
        val original = _elements.value.firstOrNull { it.id == selectedId } ?: return
        pushHistory()
        val copy = original.copy(
            id = "${original.type.name.lowercase()}_${UUID.randomUUID().toString().take(4)}",
            x = original.x + 20f,
            y = original.y + 20f,
            children = original.children.map { it.copy() }.toMutableList()
        )
        _elements.value = _elements.value + copy
        _selectedId.value = copy.id
    }

    fun saveDesign(resolver: ContentResolver, uri: Uri) {
        viewModelScope.launch {
            try {
                val json = DesignStore.serialize(_elements.value)
                withContext(Dispatchers.IO) {
                    resolver.openOutputStream(uri)?.use { out ->
                        out.write(json.toByteArray())
                    } ?: throw IOException("Cannot open output")
                }
                _exportStatus.value = "Saved OK"
            } catch (e: Exception) {
                _exportStatus.value = "Save failed: ${e.message}"
            }
        }
    }

    fun loadDesign(resolver: ContentResolver, uri: Uri) {
        viewModelScope.launch {
            try {
                val json = withContext(Dispatchers.IO) {
                    resolver.openInputStream(uri)?.use { input ->
                        input.readBytes().toString(Charsets.UTF_8)
                    } ?: throw IOException("Cannot open input")
                }
                clearHistory()
                _elements.value = DesignStore.deserialize(json)
                _selectedId.value = null
                _exportStatus.value = "Loaded OK"
            } catch (e: Exception) {
                _exportStatus.value = "Load failed: ${e.message}"
            }
        }
    }
}
