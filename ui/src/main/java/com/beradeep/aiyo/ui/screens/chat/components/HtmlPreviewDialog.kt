package com.beradeep.aiyo.ui.screens.chat.components

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.beradeep.aiyo.ui.AiyoTheme
import com.beradeep.aiyo.ui.LocalTypography
import com.beradeep.aiyo.ui.basics.components.Icon
import com.beradeep.aiyo.ui.basics.components.IconButton
import com.beradeep.aiyo.ui.basics.components.IconButtonVariant
import com.beradeep.aiyo.ui.basics.components.Text
import com.beradeep.aiyo.ui.basics.components.topbar.TopBar

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HtmlPreviewDialog(
    htmlContent: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var pendingPermissionRequest by remember { mutableStateOf<android.webkit.PermissionRequest?>(null) }
    val webViewLogs = remember { java.util.concurrent.CopyOnWriteArrayList<String>() }
    val safeDismiss = {
        if (webViewLogs.isNotEmpty()) {
            try {
                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("WebView Logs", webViewLogs.joinToString("\n"))
                clipboard.setPrimaryClip(clip)
            } catch (e: Exception) {
                android.util.Log.e("HtmlPreviewDialog", "Failed to copy logs to clipboard on dismiss: ${e.localizedMessage}")
            }
        }
        onDismiss()
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val req = pendingPermissionRequest
        if (req != null) {
            val grantedResources = mutableListOf<String>()
            for (res in req.resources) {
                when (res) {
                    android.webkit.PermissionRequest.RESOURCE_VIDEO_CAPTURE -> {
                        if (results[android.Manifest.permission.CAMERA] == true ||
                            androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        ) {
                            grantedResources.add(res)
                        }
                    }
                    android.webkit.PermissionRequest.RESOURCE_AUDIO_CAPTURE -> {
                        if (results[android.Manifest.permission.RECORD_AUDIO] == true ||
                            androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        ) {
                            grantedResources.add(res)
                        }
                    }
                    else -> {
                        grantedResources.add(res)
                    }
                }
            }
            if (grantedResources.isNotEmpty()) {
                req.grant(grantedResources.toTypedArray())
            } else {
                req.deny()
            }
        }
        pendingPermissionRequest = null
    }

    var fileChooserCallback by remember { mutableStateOf<android.webkit.ValueCallback<Array<android.net.Uri>>?>(null) }

    val fileChooserLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        val resultCode = result.resultCode
        val callback = fileChooserCallback
        if (callback != null) {
            val results = if (resultCode == android.app.Activity.RESULT_OK && data != null) {
                val dataString = data.dataString
                val clipData = data.clipData
                if (clipData != null) {
                    val count = clipData.itemCount
                    Array(count) { i -> clipData.getItemAt(i).uri }
                } else if (dataString != null) {
                    arrayOf(android.net.Uri.parse(dataString))
                } else {
                    null
                }
            } else {
                null
            }
            callback.onReceiveValue(results)
            fileChooserCallback = null
        }
    }

    Dialog(
        onDismissRequest = safeDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AiyoTheme.colors.background)
        ) {
            TopBar {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = safeDismiss,
                        variant = IconButtonVariant.PrimaryGhost
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close Preview"
                        )
                    }
                    Spacer(Modifier.width(24.dp))
                    Text("App Preview", style = LocalTypography.current.h1)
                }
            }

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        android.webkit.WebView.setWebContentsDebuggingEnabled(true)
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            useWideViewPort = false
                            loadWithOverviewMode = false
                            allowFileAccess = true
                            allowContentAccess = true
                            allowFileAccessFromFileURLs = true
                            allowUniversalAccessFromFileURLs = true
                            javaScriptCanOpenWindowsAutomatically = true
                            mediaPlaybackRequiresUserGesture = false
                            setSupportZoom(true)
                            builtInZoomControls = true
                            displayZoomControls = false
                            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            setGeolocationEnabled(true)
                        }
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                webViewLogs.add("Page Started Loading: $url")
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                webViewLogs.add("Page Finished Loading: $url")
                            }
                            override fun onReceivedError(
                                view: WebView?,
                                request: android.webkit.WebResourceRequest?,
                                error: android.webkit.WebResourceError?
                            ) {
                                super.onReceivedError(view, request, error)
                                val msg = "Error loading: ${request?.url} - Description: ${error?.description} (Code: ${error?.errorCode})"
                                android.util.Log.e("WebViewError", msg)
                                webViewLogs.add("Resource Error: $msg")
                            }

                            override fun onReceivedHttpError(
                                view: WebView?,
                                request: android.webkit.WebResourceRequest?,
                                errorResponse: android.webkit.WebResourceResponse?
                            ) {
                                super.onReceivedHttpError(view, request, errorResponse)
                                val msg = "HTTP Error loading: ${request?.url} - Status Code: ${errorResponse?.statusCode}"
                                android.util.Log.e("WebViewError", msg)
                                webViewLogs.add("HTTP Error: $msg")
                            }

                            override fun shouldInterceptRequest(
                                view: WebView?,
                                request: android.webkit.WebResourceRequest?
                            ): android.webkit.WebResourceResponse? {
                                val url = request?.url ?: return null
                                if (url.scheme == "https" && url.host == "aiyo.local") {
                                    val path = url.path ?: ""
                                    if (path == "" || path == "/" || path == "/index.html") {
                                        val polyfilledHtml = injectPolyfills(htmlContent)
                                        val response = android.webkit.WebResourceResponse(
                                            "text/html",
                                            "UTF-8",
                                            polyfilledHtml.byteInputStream()
                                        )
                                        val headers = mapOf(
                                            "Access-Control-Allow-Origin" to "*",
                                            "Access-Control-Allow-Methods" to "GET, POST, OPTIONS",
                                            "Access-Control-Allow-Headers" to "Content-Type"
                                        )
                                        response.responseHeaders = headers
                                        return response
                                    }
                                }
                                return null
                            }
                        }
                        webChromeClient = object : WebChromeClient() {
                            override fun onPermissionRequest(request: android.webkit.PermissionRequest?) {
                                if (request == null) return
                                val requiredPermissions = mutableListOf<String>()
                                for (res in request.resources) {
                                    when (res) {
                                        android.webkit.PermissionRequest.RESOURCE_VIDEO_CAPTURE -> {
                                            requiredPermissions.add(android.Manifest.permission.CAMERA)
                                        }
                                        android.webkit.PermissionRequest.RESOURCE_AUDIO_CAPTURE -> {
                                            requiredPermissions.add(android.Manifest.permission.RECORD_AUDIO)
                                        }
                                    }
                                }

                                val missingPermissions = requiredPermissions.filter {
                                    androidx.core.content.ContextCompat.checkSelfPermission(
                                        context,
                                        it
                                    ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                                }

                                if (missingPermissions.isEmpty()) {
                                    request.grant(request.resources)
                                } else {
                                    pendingPermissionRequest = request
                                    requestPermissionLauncher.launch(missingPermissions.toTypedArray())
                                }
                            }
                            override fun onGeolocationPermissionsShowPrompt(
                                origin: String?,
                                callback: android.webkit.GeolocationPermissions.Callback?
                            ) {
                                callback?.invoke(origin, true, false)
                            }
                            override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                                consoleMessage?.let {
                                    val msg = "${it.message()} -- From line ${it.lineNumber()} of ${it.sourceId()}"
                                    android.util.Log.d("WebViewConsole", msg)
                                    webViewLogs.add("Console [${it.messageLevel()}]: $msg")
                                }
                                return true
                            }
                            override fun onJsAlert(
                                view: WebView?,
                                url: String?,
                                message: String?,
                                result: android.webkit.JsResult?
                            ): Boolean {
                                android.app.AlertDialog.Builder(context)
                                    .setMessage(message)
                                    .setPositiveButton(android.R.string.ok) { _, _ -> result?.confirm() }
                                    .setCancelable(false)
                                    .create()
                                    .show()
                                return true
                            }
                            override fun onJsConfirm(
                                view: WebView?,
                                url: String?,
                                message: String?,
                                result: android.webkit.JsResult?
                            ): Boolean {
                                android.app.AlertDialog.Builder(context)
                                    .setMessage(message)
                                    .setPositiveButton(android.R.string.ok) { _, _ -> result?.confirm() }
                                    .setNegativeButton(android.R.string.cancel) { _, _ -> result?.cancel() }
                                    .setCancelable(false)
                                    .create()
                                    .show()
                                return true
                            }
                            override fun onJsPrompt(
                                view: WebView?,
                                url: String?,
                                message: String?,
                                defaultValue: String?,
                                result: android.webkit.JsPromptResult?
                            ): Boolean {
                                val input = android.widget.EditText(context).apply {
                                    setText(defaultValue)
                                }
                                android.app.AlertDialog.Builder(context)
                                    .setMessage(message)
                                    .setView(input)
                                    .setPositiveButton(android.R.string.ok) { _, _ ->
                                        result?.confirm(input.text.toString())
                                    }
                                    .setNegativeButton(android.R.string.cancel) { _, _ ->
                                        result?.cancel()
                                    }
                                    .setCancelable(false)
                                    .create()
                                    .show()
                                return true
                            }
                            override fun onShowFileChooser(
                                webView: WebView?,
                                filePathCallback: android.webkit.ValueCallback<Array<android.net.Uri>>?,
                                fileChooserParams: FileChooserParams?
                            ): Boolean {
                                fileChooserCallback?.onReceiveValue(null)
                                fileChooserCallback = filePathCallback
                                val intent = fileChooserParams?.createIntent() ?: android.content.Intent(android.content.Intent.ACTION_GET_CONTENT).apply {
                                    type = "*/*"
                                    addCategory(android.content.Intent.CATEGORY_OPENABLE)
                                    putExtra(android.content.Intent.EXTRA_ALLOW_MULTIPLE, true)
                                }
                                try {
                                    fileChooserLauncher.launch(intent)
                                } catch (e: Exception) {
                                    fileChooserCallback?.onReceiveValue(null)
                                    fileChooserCallback = null
                                    return false
                                }
                                return true
                            }
                        }
                        addJavascriptInterface(WebAppInterface(context), "AiyoAndroid")
                        loadUrl("https://aiyo.local/")
                    }
                },
                update = { _ ->
                    // Do nothing in update to prevent reloading on every recomposition
                }
            )
        }
    }
}

private fun injectPolyfills(htmlContent: String): String {
    val polyfills = """
        <script>
            (function() {
                console.log("Android WebView compatibility layer loaded");

                // Clipboard Polyfill
                var clipboardPolyfill = {
                    writeText: function(text) {
                        return new Promise(function(resolve, reject) {
                            try {
                                if (window.AiyoAndroid) {
                                    window.AiyoAndroid.copyToClipboard(text);
                                    resolve();
                                } else {
                                    reject(new Error("AiyoAndroid interface not found"));
                                }
                            } catch (e) {
                                reject(e);
                            }
                        });
                    },
                    readText: function() {
                        return new Promise(function(resolve, reject) {
                            try {
                                if (window.AiyoAndroid && window.AiyoAndroid.readFromClipboard) {
                                    resolve(window.AiyoAndroid.readFromClipboard());
                                } else {
                                    reject(new Error("Clipboard read not supported or AiyoAndroid interface not found"));
                                }
                            } catch (e) {
                                reject(e);
                            }
                        });
                    }
                };
                try {
                    Object.defineProperty(navigator, 'clipboard', {
                        value: clipboardPolyfill,
                        writable: true,
                        configurable: true
                    });
                } catch (e) {
                    navigator.clipboard = clipboardPolyfill;
                }

                // Fix viewport sizing differences
                function fixViewport() {
                    document.documentElement.style.height = window.innerHeight + "px";
                    document.body.style.height = window.innerHeight + "px";

                    var canvas = document.querySelector("canvas");
                    if (canvas) {
                        canvas.style.width = window.innerWidth + "px";
                        canvas.style.height = window.innerHeight + "px";
                    }
                }

                window.addEventListener("resize", fixViewport);
                window.addEventListener("orientationchange", fixViewport);

                // Better requestAnimationFrame fallback
                if (!window.requestAnimationFrame) {
                    window.requestAnimationFrame = function(callback) {
                        return setTimeout(function() {
                            callback(Date.now());
                        }, 16);
                    };
                }


                // Make localStorage safe
                try {
                    var testKey = '__aiyo_storage_test__';
                    window.localStorage.setItem(testKey, testKey);
                    window.localStorage.removeItem(testKey);
                } catch (e) {
                    console.warn("Standard localStorage is blocked or throws error, applying AiyoAndroid polyfill:", e);
                    var mockStorage = {
                        getItem: function(key) {
                            return window.AiyoAndroid ? window.AiyoAndroid.getItem(key) : null;
                        },
                        setItem: function(key, value) {
                            if (window.AiyoAndroid) window.AiyoAndroid.setItem(key, String(value));
                        },
                        removeItem: function(key) {
                            if (window.AiyoAndroid) window.AiyoAndroid.removeItem(key);
                        },
                        clear: function() {
                            if (window.AiyoAndroid) window.AiyoAndroid.clear();
                        },
                        key: function(index) {
                            if (!window.AiyoAndroid) return null;
                            try {
                                var keys = JSON.parse(window.AiyoAndroid.getKeys());
                                return keys[index] || null;
                            } catch(err) {
                                return null;
                            }
                        }
                    };
                    Object.defineProperty(mockStorage, 'length', {
                        get: function() {
                            if (!window.AiyoAndroid) return 0;
                            try {
                                var keys = JSON.parse(window.AiyoAndroid.getKeys());
                                return keys.length;
                            } catch(err) {
                                return 0;
                            }
                        }
                    });
                    try {
                        Object.defineProperty(window, 'localStorage', {
                            value: mockStorage,
                            writable: true,
                            configurable: true
                        });
                    } catch (err) {
                        window.localStorage = mockStorage;
                    }
                }

                // Capture hidden JS errors
                window.onerror = function(msg, url, line, col, error) {
                    console.log("JS ERROR:", msg, "line:", line);
                };

                setTimeout(fixViewport, 100);
            })();
        </script>
    """.trimIndent()

    val injection = "\n" + polyfills + "\n"

    return if (htmlContent.contains("<head>", ignoreCase = true)) {
        htmlContent.replaceFirst("<head>", "<head>" + injection, ignoreCase = true)
    } else if (htmlContent.contains("<html>", ignoreCase = true)) {
        htmlContent.replaceFirst("<html>", "<html><head>" + injection + "</head>", ignoreCase = true)
    } else {
        injection + htmlContent
    }
}

private class WebAppInterface(private val context: android.content.Context) {
    private val sharedPrefs = context.getSharedPreferences("aiyo_webview_storage", android.content.Context.MODE_PRIVATE)

    @android.webkit.JavascriptInterface
    fun copyToClipboard(text: String) {
        (context as? android.app.Activity)?.runOnUiThread {
            try {
                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Web App Copied", text)
                clipboard.setPrimaryClip(clip)
            } catch (e: Exception) {
                android.util.Log.e("WebAppInterface", "Failed to copy to clipboard: ${e.localizedMessage}")
            }
        }
    }

    @android.webkit.JavascriptInterface
    fun readFromClipboard(): String {
        return try {
            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            if (clipboard.hasPrimaryClip()) {
                val item = clipboard.primaryClip?.getItemAt(0)
                item?.text?.toString() ?: ""
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    @android.webkit.JavascriptInterface
    fun getItem(key: String): String? {
        return sharedPrefs.getString(key, null)
    }

    @android.webkit.JavascriptInterface
    fun setItem(key: String, value: String) {
        sharedPrefs.edit().putString(key, value).apply()
    }

    @android.webkit.JavascriptInterface
    fun removeItem(key: String) {
        sharedPrefs.edit().remove(key).apply()
    }

    @android.webkit.JavascriptInterface
    fun clear() {
        sharedPrefs.edit().clear().apply()
    }

    @android.webkit.JavascriptInterface
    fun getKeys(): String {
        val keys = sharedPrefs.all.keys
        return org.json.JSONArray(keys).toString()
    }
}
