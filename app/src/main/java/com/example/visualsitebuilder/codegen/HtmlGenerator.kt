// Converts an element tree into a standalone HTML document.
package com.example.visualsitebuilder.codegen

import com.example.visualsitebuilder.model.DesignElement
import com.example.visualsitebuilder.model.ElementType

object HtmlGenerator {
    fun generate(
        elements: List<DesignElement>,
        imageExt: Map<String, String> = emptyMap()
    ): String {
        val body = elements.joinToString("\n") { renderElement(it, imageExt) }
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

    private fun renderElement(el: DesignElement, imageExt: Map<String, String>): String = when (el.type) {
        ElementType.TEXT -> """<p id="${el.id}" class="el-${el.id}">${escapeHtml(el.text)}</p>"""
        ElementType.IMAGE -> """<img id="${el.id}" class="el-${el.id}" src="${imageFile(el, imageExt)}" />"""
        ElementType.BUTTON -> """<button id="${el.id}" class="el-${el.id}">${escapeHtml(el.text)}</button>"""
        ElementType.CONTAINER -> """<div id="${el.id}" class="el-${el.id}">${el.children.joinToString("\n") { renderElement(it, imageExt) }}</div>"""
    }

    private fun imageFile(el: DesignElement, imageExt: Map<String, String>): String =
        "images/${el.id}.${imageExt[el.id] ?: "png"}"

    private fun escapeHtml(value: String): String =
        value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")

    fun generateStandalone(
        elements: List<DesignElement>,
        imageSrc: Map<String, String> = emptyMap()
    ): String {
        val body = elements.joinToString("\n") { renderStandalone(it, imageSrc) }
        val css = CssGenerator.generate(elements)
        return """
            <!DOCTYPE html>
            <html lang="ar">
            <head>
                <meta charset="UTF-8">
                <style>
                $css
                </style>
                <title>My Site</title>
            </head>
            <body>
            $body
            </body>
            </html>
        """.trimIndent()
    }

    private fun renderStandalone(el: DesignElement, imageSrc: Map<String, String>): String = when (el.type) {
        ElementType.IMAGE -> """<img id="${el.id}" class="el-${el.id}" src="${imageSrc[el.id] ?: "images/${el.id}.png"}" />"""
        ElementType.CONTAINER -> """<div id="${el.id}" class="el-${el.id}">${el.children.joinToString("\n") { renderStandalone(it, imageSrc) }}</div>"""
        else -> renderElement(el, emptyMap())
    }
}
