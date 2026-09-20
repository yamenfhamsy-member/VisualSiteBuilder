// Entry activity hosting the design canvas.
package com.example.visualsitebuilder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.visualsitebuilder.engine.CanvasState
import com.example.visualsitebuilder.ui.DesignCanvas

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val canvasState: CanvasState = viewModel()
                    val elements by canvasState.elements.collectAsState()
                    val selectedId by canvasState.selectedId.collectAsState()
                    DesignCanvas(
                        elements = elements,
                        selectedId = selectedId,
                        onElementMoved = { id, dx, dy -> canvasState.moveElement(id, dx, dy) },
                        onElementResized = { id, dx, dy -> canvasState.resizeElement(id, dx, dy) },
                        onElementSelected = { id -> canvasState.selectElement(id) }
                    )
                }
            }
        }
    }
}
