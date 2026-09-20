// Toolbox panel with buttons adding new canvas elements.
package com.example.visualsitebuilder.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.visualsitebuilder.model.ElementType

@Composable
fun ToolboxPanel(onAddElement: (ElementType) -> Unit) {
    Column(
        modifier = Modifier.fillMaxHeight().width(110.dp).padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "Toolbox")
        ElementType.entries.forEach { type ->
            Button(onClick = { onAddElement(type) }) {
                Text(text = type.name)
            }
        }
    }
}
