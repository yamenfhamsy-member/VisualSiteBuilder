// Final read-only preview of the generated site (no editing here).
package com.example.visualsitebuilder.ui

import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

class PreviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val html = intent.getStringExtra(EXTRA_HTML) ?: "<html><body>No content</body></html>"
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PreviewWebView(html = html)
                }
            }
        }
    }

    companion object {
        const val EXTRA_HTML = "extra_html"
    }
}

@Composable
private fun PreviewWebView(html: String) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = false
                loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
            }
        }
    )
}
