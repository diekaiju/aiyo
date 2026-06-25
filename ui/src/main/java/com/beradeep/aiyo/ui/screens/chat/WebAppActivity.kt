package com.beradeep.aiyo.ui.screens.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.beradeep.aiyo.ui.screens.chat.components.AndroidDBInterface
import java.io.File

class WebAppActivity : ComponentActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val filePath = intent.getStringExtra("FILE_PATH")
        val conversationId = intent.getStringExtra("CONVERSATION_ID") ?: ""

        val htmlContent = if (filePath != null) {
            val file = File(filePath)
            if (file.exists()) file.readText() else "<h1>Error: Web app file missing</h1>"
        } else {
            "<h1>Error: No file path provided</h1>"
        }

        setContent {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.useWideViewPort = true
                        settings.loadWithOverviewMode = true
                        webViewClient = WebViewClient()
                        webChromeClient = WebChromeClient()
                        if (conversationId.isNotBlank()) {
                            addJavascriptInterface(AndroidDBInterface(context, conversationId), "AndroidDB")
                        }
                    }
                },
                update = { webView ->
                    webView.settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    webView.loadDataWithBaseURL("https://localhost/", htmlContent, "text/html", "UTF-8", null)
                }
            )
        }
    }
}
