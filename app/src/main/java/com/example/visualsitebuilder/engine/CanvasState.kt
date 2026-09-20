// Canvas state holder (ViewModel) exposing immutable StateFlow.
package com.example.visualsitebuilder.engine

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.visualsitebuilder.codegen.CssGenerator
import com.example.visualsitebuilder.codegen.HtmlGenerator
import com.example.visualsitebuilder.export.ZipExporter
import com.example.visualsitebuilder.model.DesignElement
import com.example.visualsitebuilder.model.ElementType
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    fun updateElement(updated: DesignElement) {
        _elements.value = _elements.value.map { el ->
            if (el.id == updated.id) updated else el
        }
    }

    private val _exportStatus = MutableStateFlow<String?>(null)
    val exportStatus: StateFlow<String?> = _exportStatus.asStateFlow()

    fun exportSite(resolver: ContentResolver, uri: Uri) {
        viewModelScope.launch {
            try {
                val html = HtmlGenerator.generate(_elements.value)
                val css = CssGenerator.generate(_elements.value)
                resolver.openOutputStream(uri)?.use { out ->
                    ZipExporter.exportToStream(out, html, css)
                } ?: throw IOException("Cannot open output")
                _exportStatus.value = "Exported OK"
            } catch (e: Exception) {
                _exportStatus.value = "Export failed: ${e.message}"
            }
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
        _elements.value = _elements.value + element
        _selectedId.value = id
    }

    fun deleteSelected() {
        val id = _selectedId.value ?: return
        _elements.value = _elements.value.filter { it.id != id }
        _selectedId.value = null
    }

    fun duplicateSelected() {
        val selectedId = _selectedId.value ?: return
        val original = _elements.value.firstOrNull { it.id == selectedId } ?: return
        val copy = original.copy(
            id = "${original.type.name.lowercase()}_${UUID.randomUUID().toString().take(4)}",
            x = original.x + 20f,
            y = original.y + 20f,
            children = original.children.map { it.copy() }.toMutableList()
        )
        _elements.value = _elements.value + copy
        _selectedId.value = copy.id
    }
}
