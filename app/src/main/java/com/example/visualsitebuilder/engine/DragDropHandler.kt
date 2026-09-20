// Pure drag-drop position math helpers.
package com.example.visualsitebuilder.engine

object DragDropHandler {
    fun applyDrag(x: Float, y: Float, dx: Float, dy: Float): Pair<Float, Float> {
        val newX = (x + dx).coerceAtLeast(0f)
        val newY = (y + dy).coerceAtLeast(0f)
        return newX to newY
    }

    fun applyResize(width: Float, height: Float, dx: Float, dy: Float): Pair<Float, Float> {
        val newWidth = (width + dx).coerceIn(40f, 2000f)
        val newHeight = (height + dy).coerceIn(40f, 2000f)
        return newWidth to newHeight
    }
}
