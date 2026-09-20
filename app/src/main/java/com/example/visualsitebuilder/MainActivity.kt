// Entry activity hosting toolbox, canvas, properties and export.
package com.example.visualsitebuilder

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.visualsitebuilder.codegen.HtmlGenerator
import com.example.visualsitebuilder.engine.CanvasState
import com.example.visualsitebuilder.ui.DesignCanvas
import com.example.visualsitebuilder.ui.PreviewActivity
import com.example.visualsitebuilder.ui.PropertiesPanel
import com.example.visualsitebuilder.ui.ToolboxPanel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val canvasState: CanvasState = viewModel()
                    val elements by canvasState.elements.collectAsState()
                    val selectedId by canvasState.selectedId.collectAsState()
                    val exportStatus by canvasState.exportStatus.collectAsState()
                    val context = LocalContext.current
                    val exportLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.CreateDocument("application/zip")
                    ) { uri ->
                        uri?.let { canvasState.exportSite(context.contentResolver, it) }
                    }
                    val saveLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.CreateDocument("application/json")
                    ) { uri ->
                        uri?.let { canvasState.saveDesign(context.contentResolver, it) }
                    }
                    val openLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.OpenDocument()
                    ) { uri ->
                        uri?.let { canvasState.loadDesign(context.contentResolver, it) }
                    }
                    exportStatus?.let { msg ->
                        LaunchedEffect(msg) {
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            canvasState.clearExportStatus()
                        }
                    }
                    val selected = elements.firstOrNull { it.id == selectedId }
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "VisualSiteBuilder")
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { saveLauncher.launch("design.json") }) {
                                    Text(text = "Save")
                                }
                                Button(onClick = { openLauncher.launch(arrayOf("application/json")) }) {
                                    Text(text = "Open")
                                }
                                Button(
                                    onClick = {
                                        val html = HtmlGenerator.generateStandalone(elements)
                                        val intent = Intent(context, PreviewActivity::class.java)
                                        intent.putExtra(PreviewActivity.EXTRA_HTML, html)
                                        context.startActivity(intent)
                                    }
                                ) {
                                    Text(text = "Preview")
                                }
                                Button(onClick = { exportLauncher.launch("mysite.zip") }) {
                                    Text(text = "Export")
                                }
                            }
                        }
                        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            ToolboxPanel(
                                onAddElement = { type -> canvasState.addElement(type) }
                            )
                            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                DesignCanvas(
                                    elements = elements,
                                    selectedId = selectedId,
                                    onElementMoved = { id, dx, dy -> canvasState.moveElement(id, dx, dy) },
                                    onElementResized = { id, dx, dy -> canvasState.resizeElement(id, dx, dy) },
                                    onElementSelected = { id -> canvasState.selectElement(id) }
                                )
                            }
                            PropertiesPanel(
                                element = selected,
                                onUpdateElement = { updated -> canvasState.updateElement(updated) },
                                onDeleteElement = { canvasState.deleteSelected() },
                                onDuplicateElement = { canvasState.duplicateSelected() }
                            )
                        }
                    }
                }
            }
        }
    }
}
