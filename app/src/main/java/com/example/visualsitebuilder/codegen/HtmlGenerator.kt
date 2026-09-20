// Converts an element tree into HTML documents (linked or standalone).
package com.example.visualsitebuilder.codegen

import com.example.visualsitebuilder.model.DesignElement
import com.example.visualsitebuilder.model.ElementType

object HtmlGenerator {
    fun generate(
        elements: List<DesignElement>,
        imageExt: Map<String, String> = emptyMap()
    ): String {
        val body = elements.joinToString("\n") { renderElement(it, imageExt, emptyMap()) }
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

    fun generateStandalone(
        elements: List<DesignElement>,
        imageSrc: Map<String, String> = emptyMap()
    ): String {
        val body = elements.joinToString("\n") { renderElement(it, emptyMap(), imageSrc) }
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

    private fun renderElement(
        el: DesignElement,
        imageExt: Map<String, String>,
        imageSrc: Map<String, String>
    ): String {
        val tag = el.type.htmlTag
        val attrs = """id="${el.id}" class="el-${el.id}""""
        val kids = el.children.joinToString("\n") { renderElement(it, imageExt, imageSrc) }
        return when (el.type) {
            ElementType.IMAGE -> {
                val src = imageSrc[el.id] ?: imageFile(el, imageExt)
                """<img $attrs src="$src" />"""
            }
            ElementType.INPUT -> {
                """<input $attrs type="text" placeholder="${escapeHtml(el.hint)}" value="${escapeHtml(el.text)}" />"""
            }
            ElementType.DIVIDER -> """<hr $attrs />"""
            ElementType.LINK -> {
                val href = escapeHtml(el.linkUrl.ifBlank { "#" })
                """<a $attrs href="$href">${escapeHtml(el.text)}</a>"""
            }
            ElementType.VIDEO -> {
                val src = escapeHtml(el.linkUrl)
                """<video $attrs src="$src" controls>$kids</video>"""
            }
            ElementType.TEXTAREA -> {
                """<textarea $attrs placeholder="${escapeHtml(el.hint)}">${escapeHtml(el.text)}</textarea>"""
            }
            else -> {
                val inner = if (ElementType.isContainer(el.type)) {
                    kids
                } else {
                    escapeHtml(el.text) + (if (kids.isNotEmpty()) "\n$kids" else "")
                }
                """<$tag $attrs>$inner</$tag>"""
            }
        }
    }

    private fun imageFile(el: DesignElement, imageExt: Map<String, String>): String =
        "images/${el.id}.${imageExt[el.id] ?: "png"}"

    private fun escapeHtml(value: String): String =
        value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
}
