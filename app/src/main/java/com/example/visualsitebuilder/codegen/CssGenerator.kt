// Converts element properties (including nested children) into CSS.
package com.example.visualsitebuilder.codegen

import com.example.visualsitebuilder.model.DesignElement

object CssGenerator {
    fun generate(elements: List<DesignElement>): String =
        flatten(elements).joinToString("\n") { el ->
            """
            .el-${el.id} {
                position: absolute;
                left: ${el.x}px;
                top: ${el.y}px;
                width: ${el.width}px;
                height: ${el.height}px;
                background-color: ${el.backgroundColor};
                color: ${el.textColor};
                font-size: ${el.fontSize}px;
            }
            """.trimIndent()
        }

    private fun flatten(elements: List<DesignElement>): List<DesignElement> =
        elements.flatMap { listOf(it) + flatten(it.children) }
}
