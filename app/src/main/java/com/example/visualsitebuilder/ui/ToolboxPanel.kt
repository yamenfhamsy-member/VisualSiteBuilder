// Toolbox panel with categorized buttons adding new canvas elements.
package com.example.visualsitebuilder.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.visualsitebuilder.model.ElementType

private val toolboxGroups: List<Pair<String, List<ElementType>>> = listOf(
    "Text" to listOf(
        ElementType.TEXT,
        ElementType.HEADING_1,
        ElementType.HEADING_2,
        ElementType.HEADING_3,
        ElementType.HEADING_4,
        ElementType.HEADING_5,
        ElementType.HEADING_6,
        ElementType.LINK,
        ElementType.LABEL,
        ElementType.DIVIDER
    ),
    "Media" to listOf(ElementType.IMAGE, ElementType.VIDEO),
    "Controls" to listOf(ElementType.BUTTON, ElementType.INPUT, ElementType.TEXTAREA),
    "Layout" to listOf(
        ElementType.CONTAINER,
        ElementType.SECTION,
        ElementType.HEADER,
        ElementType.FOOTER,
        ElementType.NAV,
        ElementType.MAIN,
        ElementType.ARTICLE
    ),
    "Lists & Forms" to listOf(
        ElementType.LIST,
        ElementType.ORDERED_LIST,
        ElementType.LIST_ITEM,
        ElementType.FORM,
        ElementType.TABLE
    )
)

@Composable
fun ToolboxPanel(onAddElement: (ElementType) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(150.dp)
            .padding(8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = "Toolbox", style = MaterialTheme.typography.titleSmall)
        toolboxGroups.forEach { (title, types) ->
            Text(text = title, style = MaterialTheme.typography.labelLarge)
            types.forEach { type ->
                Button(
                    onClick = { onAddElement(type) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = type.label)
                }
            }
        }
    }
}
