// Writes generated site files into a zip archive.
package com.example.visualsitebuilder.export

import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ZipExporter {
    suspend fun exportToStream(output: OutputStream, html: String, css: String) {
        withContext(Dispatchers.IO) {
            ZipOutputStream(output).use { zip ->
                zip.putNextEntry(ZipEntry("index.html"))
                zip.write(html.toByteArray())
                zip.closeEntry()

                zip.putNextEntry(ZipEntry("style.css"))
                zip.write(css.toByteArray())
                zip.closeEntry()
            }
        }
    }
}
