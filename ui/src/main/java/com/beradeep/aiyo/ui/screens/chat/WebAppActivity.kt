package com.beradeep.aiyo.ui.screens.chat

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
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
    private var pendingPermissionRequest: android.webkit.PermissionRequest? = null
    private var fileChooserCallback: android.webkit.ValueCallback<Array<android.net.Uri>>? = null
    private val webViewLogs = java.util.concurrent.CopyOnWriteArrayList<String>()
    private var isLogsCopied = false

    private val fileChooserLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
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

    private val requestPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val req = pendingPermissionRequest
        if (req != null) {
            val grantedResources = mutableListOf<String>()
            for (res in req.resources) {
                when (res) {
                    android.webkit.PermissionRequest.RESOURCE_VIDEO_CAPTURE -> {
                        if (results[android.Manifest.permission.CAMERA] == true ||
                            androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        ) {
                            grantedResources.add(res)
                        }
                    }
                    android.webkit.PermissionRequest.RESOURCE_AUDIO_CAPTURE -> {
                        if (results[android.Manifest.permission.RECORD_AUDIO] == true ||
                            androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED
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

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val windowInsetsController = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())

        val filePath = intent.getStringExtra("FILE_PATH")
        var targetHtmlFile: File? = null
        var targetAppDirectory: File? = null

        var htmlContent = when {
            filePath != null -> {
                val file = File(filePath)
                if (file.exists()) {
                    targetHtmlFile = file
                    targetAppDirectory = file.parentFile
                    null
                } else {
                    "<h1>Error: Web app file missing</h1>"
                }
            }
            intent.action == Intent.ACTION_VIEW -> {
                val uri: Uri? = intent.data
                if (uri != null) {
                    if (uri.scheme == "file") {
                        val file = uri.path?.let { File(it) }
                        if (file != null && file.exists()) {
                            targetHtmlFile = file
                            targetAppDirectory = file.parentFile
                            null
                        } else {
                            "<h1>Error reading local file: File not found or invalid path</h1>"
                        }
                    } else {
                        try {
                            contentResolver.openInputStream(uri)?.use { inputStream ->
                                inputStream.bufferedReader().use { it.readText() }
                            } ?: "<h1>Error: Could not open input stream for shared file</h1>"
                        } catch (e: Exception) {
                            "<h1>Error reading shared file: ${e.localizedMessage}</h1>"
                        }
                    }
                } else {
                    "<h1>Error: No data URI provided in VIEW intent</h1>"
                }
            }
            intent.action == Intent.ACTION_SEND -> {
                val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                val streamUri = if (android.os.Build.VERSION.SDK_INT >= 33) {
                    intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(Intent.EXTRA_STREAM) as? Uri
                }

                when {
                    sharedText != null -> sharedText
                    streamUri != null -> {
                        try {
                            contentResolver.openInputStream(streamUri)?.use { inputStream ->
                                inputStream.bufferedReader().use { it.readText() }
                            } ?: "<h1>Error: Could not open input stream for shared content</h1>"
                        } catch (e: Exception) {
                            "<h1>Error reading shared content: ${e.localizedMessage}</h1>"
                        }
                    }
                    else -> "<h1>Error: Shared content has no text or stream URI</h1>"
                }
            }
            else -> {
                "<h1>Error: No web app file path or shared content provided</h1>"
            }
        }

        setContent {
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
                                        val content = when {
                                            htmlContent != null -> htmlContent!!
                                            targetHtmlFile != null && targetHtmlFile.exists() -> {
                                                try {
                                                    targetHtmlFile.readText()
                                                } catch (e: Exception) {
                                                    "<h1>Error reading HTML file: ${e.localizedMessage}</h1>"
                                                }
                                            }
                                            else -> "<h1>Error: No content or file to load</h1>"
                                        }
                                        val polyfilledHtml = injectPolyfills(content)
                                        return createResponse("text/html", polyfilledHtml.byteInputStream())
                                    } else {
                                        val appDir = targetAppDirectory
                                        if (appDir != null && appDir.exists()) {
                                            val relativePath = if (path.startsWith("/")) path.substring(1) else path
                                            val localFile = File(appDir, relativePath)
                                            try {
                                                val canonicalAppDir = appDir.canonicalPath + File.separator
                                                if (localFile.exists() && localFile.isFile &&
                                                    localFile.canonicalPath.startsWith(canonicalAppDir)
                                                ) {
                                                    val mime = getMimeType(localFile.name)
                                                    return createResponse(mime, localFile.inputStream())
                                                }
                                            } catch (e: Exception) {
                                                android.util.Log.e("WebViewIntercept", "Error serving local file: ${e.localizedMessage}")
                                            }
                                        }
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
                                        this@WebAppActivity,
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

    private fun getMimeType(url: String): String {
        return when {
            url.endsWith(".html", ignoreCase = true) || url.endsWith(".htm", ignoreCase = true) -> "text/html"
            url.endsWith(".js", ignoreCase = true) || url.endsWith(".mjs", ignoreCase = true) -> "application/javascript"
            url.endsWith(".css", ignoreCase = true) -> "text/css"
            url.endsWith(".json", ignoreCase = true) -> "application/json"
            url.endsWith(".png", ignoreCase = true) -> "image/png"
            url.endsWith(".jpg", ignoreCase = true) || url.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
            url.endsWith(".gif", ignoreCase = true) -> "image/gif"
            url.endsWith(".svg", ignoreCase = true) -> "image/svg+xml"
            url.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
            url.endsWith(".wasm", ignoreCase = true) -> "application/wasm"
            url.endsWith(".xml", ignoreCase = true) -> "application/xml"
            url.endsWith(".txt", ignoreCase = true) -> "text/plain"
            url.endsWith(".woff", ignoreCase = true) -> "font/woff"
            url.endsWith(".woff2", ignoreCase = true) -> "font/woff2"
            url.endsWith(".ttf", ignoreCase = true) -> "font/ttf"
            url.endsWith(".otf", ignoreCase = true) -> "font/otf"
            else -> "application/octet-stream"
        }
    }

    private fun createResponse(mimeType: String, data: java.io.InputStream): android.webkit.WebResourceResponse {
        val isBinary = when {
            mimeType.startsWith("image/", ignoreCase = true) -> true
            mimeType.startsWith("font/", ignoreCase = true) -> true
            mimeType.equals("application/wasm", ignoreCase = true) -> true
            mimeType.equals("application/octet-stream", ignoreCase = true) -> true
            mimeType.equals("application/pdf", ignoreCase = true) -> true
            else -> false
        }
        val encoding = if (isBinary) null else "UTF-8"
        val response = android.webkit.WebResourceResponse(mimeType, encoding, data)
        val headers = mutableMapOf<String, String>()
        headers["Access-Control-Allow-Origin"] = "*"
        headers["Access-Control-Allow-Methods"] = "GET, POST, OPTIONS"
        headers["Access-Control-Allow-Headers"] = "Content-Type"
        response.responseHeaders = headers
        return response
    }

    private fun copyLogsToClipboard() {
        if (!isLogsCopied && webViewLogs.isNotEmpty()) {
            isLogsCopied = true
            try {
                val clipboard = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("WebView Logs", webViewLogs.joinToString("\n"))
                clipboard.setPrimaryClip(clip)
            } catch (e: Exception) {
                android.util.Log.e("WebAppActivity", "Failed to copy logs to clipboard: ${e.localizedMessage}")
            }
        }
    }

    override fun finish() {
        copyLogsToClipboard()
        super.finish()
    }

    override fun onDestroy() {
        copyLogsToClipboard()
        super.onDestroy()
    }

    class WebAppInterface(private val context: android.content.Context) {
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
}
