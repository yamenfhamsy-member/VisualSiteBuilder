// Canvas state holder (ViewModel) exposing immutable StateFlow.
package com.example.visualsitebuilder.engine

import androidx.lifecycle.ViewModel
import com.example.visualsitebuilder.model.DesignElement
import com.example.visualsitebuilder.model.ElementType
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
}
