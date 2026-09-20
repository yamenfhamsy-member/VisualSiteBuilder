// Converts an element tree into a standalone HTML document.
package com.example.visualsitebuilder.codegen

import com.example.visualsitebuilder.model.DesignElement
import com.example.visualsitebuilder.model.ElementType

object HtmlGenerator {
    fun generate(elements: List<DesignElement>): String {
        val body = elements.joinToString("\n") { renderElement(it) }
        return """
            <!DOCTYPE html>
            <html lang="ar">
            <head>
                <meta charset="UTF-8">
                <link rel="stylesheet" href="style.css">
                <title>My Site</title>
            </head>
            <body>
            $body
            </body>
            </html>
        """.trimIndent()
    }

    private fun renderElement(el: DesignElement): String = when (el.type) {
        ElementType.TEXT -> """<p id="${el.id}" class="el-${el.id}">${escapeHtml(el.text)}</p>"""
        ElementType.IMAGE -> """<img id="${el.id}" class="el-${el.id}" src="images/${el.id}.png" />"""
        ElementType.BUTTON -> """<button id="${el.id}" class="el-${el.id}">${escapeHtml(el.text)}</button>"""
        ElementType.CONTAINER -> """<div id="${el.id}" class="el-${el.id}">${el.children.joinToString("\n") { renderElement(it) }}</div>"""
    }

    private fun escapeHtml(value: String): String =
        value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
}
