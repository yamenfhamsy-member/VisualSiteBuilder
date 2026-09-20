// Data model for a single design canvas element.
package com.example.visualsitebuilder.model

data class DesignElement(
    val id: String,
    val type: ElementType,
    var x: Float,
    var y: Float,
    var width: Float,
    var height: Float,
    var text: String = "",
    var backgroundColor: String = "#FFFFFF",
    var textColor: String = "#000000",
    var fontSize: Int = 16,
    val children: MutableList<DesignElement> = mutableListOf()
)
