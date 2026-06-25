package com.beradeep.aiyo.ui.screens.chat.components

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    conversationId: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
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
                        onClick = onDismiss,
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

class AndroidDBInterface(private val context: android.content.Context, private val conversationId: String) {
    @android.webkit.JavascriptInterface
    fun saveData(json: String) {
        val sharedPreferences = context.getSharedPreferences("AndroidDB_$conversationId", android.content.Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("data", json).apply()
    }

    @android.webkit.JavascriptInterface
    fun loadData(): String {
        val sharedPreferences = context.getSharedPreferences("AndroidDB_$conversationId", android.content.Context.MODE_PRIVATE)
        return sharedPreferences.getString("data", "{}") ?: "{}"
    }
}
