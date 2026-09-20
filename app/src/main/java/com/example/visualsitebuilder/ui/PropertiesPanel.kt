// Properties panel editing the currently selected element.
package com.example.visualsitebuilder.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.visualsitebuilder.model.DesignElement
import com.example.visualsitebuilder.model.ElementType

@Composable
fun PropertiesPanel(
    element: DesignElement?,
    onUpdateElement: (DesignElement) -> Unit,
    onDeleteElement: () -> Unit,
    onDuplicateElement: () -> Unit,
    onImagePicked: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(210.dp)
            .padding(8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "Properties")
        val context = LocalContext.current
        val pickImageLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            uri?.let {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: SecurityException) {
                    // Persistence not granted, transient access still works.
                }
                onImagePicked(it.toString())
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onDuplicateElement, enabled = element != null) {
                Text(text = "Duplicate")
            }
            Button(onClick = onDeleteElement, enabled = element != null) {
                Text(text = "Delete")
            }
        }
        if (element == null) {
            Text(text = "Select an element")
            return
        }
        if (element.type == ElementType.IMAGE) {
            Button(onClick = { pickImageLauncher.launch(arrayOf("image/*")) }) {
                Text(text = "Pick image")
            }
        }
        key(element.id) {
            var text by remember { mutableStateOf(element.text) }
            var bg by remember { mutableStateOf(element.backgroundColor) }
            var fg by remember { mutableStateOf(element.textColor) }
            var fontSize by remember { mutableStateOf(element.fontSize.toString()) }
            var width by remember { mutableStateOf(element.width.toString()) }
            var height by remember { mutableStateOf(element.height.toString()) }

            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    onUpdateElement(element.copy(text = it))
                },
                label = { Text("Text") },
                singleLine = true
            )
            OutlinedTextField(
                value = bg,
                onValueChange = {
                    bg = it
                    if (isColorInputValid(it)) {
                        onUpdateElement(element.copy(backgroundColor = it))
                    }
                },
                label = { Text("Bg #RRGGBB") },
                singleLine = true
            )
            OutlinedTextField(
                value = fg,
                onValueChange = {
                    fg = it
                    if (isColorInputValid(it)) {
                        onUpdateElement(element.copy(textColor = it))
                    }
                },
                label = { Text("Text #RRGGBB") },
                singleLine = true
            )
            OutlinedTextField(
                value = fontSize,
                onValueChange = {
                    fontSize = it
                    it.toIntOrNull()?.let { size ->
                        if (size in 8..120) onUpdateElement(element.copy(fontSize = size))
                    }
                },
                label = { Text("Font size") },
                singleLine = true
            )
            OutlinedTextField(
                value = width,
                onValueChange = {
                    width = it
                    it.toFloatOrNull()?.let { w ->
                        if (w in 40f..2000f) onUpdateElement(element.copy(width = w))
                    }
                },
                label = { Text("Width") },
                singleLine = true
            )
            OutlinedTextField(
                value = height,
                onValueChange = {
                    height = it
                    it.toFloatOrNull()?.let { h ->
                        if (h in 40f..2000f) onUpdateElement(element.copy(height = h))
                    }
                },
                label = { Text("Height") },
                singleLine = true
            )
        }
    }
}

private fun isColorInputValid(value: String): Boolean {
    if (value.length != 7 || !value.startsWith("#")) return false
    return try {
        android.graphics.Color.parseColor(value)
        true
    } catch (e: IllegalArgumentException) {
        false
    }
}
