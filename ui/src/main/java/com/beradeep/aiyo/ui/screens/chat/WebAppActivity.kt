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
import java.io.File

class WebAppActivity : ComponentActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val filePath = intent.getStringExtra("FILE_PATH")

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
                        setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            allowFileAccess = true
                            allowContentAccess = true
                            javaScriptCanOpenWindowsAutomatically = true
                            mediaPlaybackRequiresUserGesture = false
                            setSupportZoom(true)
                            builtInZoomControls = true
                            displayZoomControls = false
                            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            setGeolocationEnabled(true)
                        }
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                view?.evaluateJavascript(
                                    """
                                    (function() {
                                        function forceZoom() {
                                            var metas = document.getElementsByTagName('meta');
                                            var foundViewport = false;
                                            for (var i = 0; i < metas.length; i++) {
                                                if (metas[i].getAttribute('name') === 'viewport') {
                                                    foundViewport = true;
                                                    var content = metas[i].getAttribute('content') || '';
                                                    var originalContent = content;
                                                    content = content.replace(/user-scalable\s*=\s*(no|0|false)/gi, 'user-scalable=yes');
                                                    content = content.replace(/maximum-scale\s*=\s*[0-9.]+/gi, 'maximum-scale=5.0');
                                                    if (content.indexOf('user-scalable=yes') === -1) {
                                                        content += ', user-scalable=yes';
                                                    }
                                                    if (content.indexOf('maximum-scale') === -1) {
                                                        content += ', maximum-scale=5.0';
                                                    }
                                                    if (content !== originalContent) {
                                                        metas[i].setAttribute('content', content);
                                                    }
                                                }
                                            }
                                            if (!foundViewport) {
                                                var meta = document.createElement('meta');
                                                meta.name = 'viewport';
                                                meta.content = 'width=device-width, initial-scale=1.0, maximum-scale=5.0, user-scalable=yes';
                                                document.getElementsByTagName('head')[0].appendChild(meta);
                                            }
                                        }

                                        forceZoom();

                                        var observer = new MutationObserver(function(mutations) {
                                            forceZoom();
                                        });
                                        observer.observe(document.documentElement, {
                                            childList: true,
                                            subtree: true,
                                            attributes: true,
                                            attributeFilter: ['content', 'name']
                                        });
                                    })();
                                    """.trimIndent(),
                                    null
                                )
                            }
                        }
                        webChromeClient = object : WebChromeClient() {
                            override fun onPermissionRequest(request: android.webkit.PermissionRequest?) {
                                request?.grant(request.resources)
                            }
                            override fun onGeolocationPermissionsShowPrompt(
                                origin: String?,
                                callback: android.webkit.GeolocationPermissions.Callback?
                            ) {
                                callback?.invoke(origin, true, false)
                            }
                            override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                                android.util.Log.d(
                                    "WebViewConsole",
                                    "${consoleMessage?.message()} -- From line ${consoleMessage?.lineNumber()} of ${consoleMessage?.sourceId()}"
                                )
                                return true
                            }
                        }
                        loadDataWithBaseURL("https://localhost/", htmlContent, "text/html", "UTF-8", null)
                    }
                },
                update = { _ ->
                    // Do nothing in update to prevent reloading on every recomposition
                }
            )
        }
    }
}
