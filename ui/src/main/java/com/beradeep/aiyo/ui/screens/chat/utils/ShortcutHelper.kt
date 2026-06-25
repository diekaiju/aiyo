package com.beradeep.aiyo.ui.screens.chat.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.beradeep.aiyo.ui.R
import com.beradeep.aiyo.ui.screens.chat.WebAppActivity
import java.io.File
import java.util.UUID

object ShortcutHelper {
    fun addWebAppToHomeScreen(context: Context, htmlContent: String, conversationId: String) {
        // Save HTML to a local file
        val filename = "webapp_${UUID.randomUUID()}.html"
        val file = File(context.filesDir, filename)
        file.writeText(htmlContent)

        // Create Intent
        val intent = Intent(context, WebAppActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra("FILE_PATH", file.absolutePath)
            putExtra("CONVERSATION_ID", conversationId)
        }

        // Create Shortcut
        val shortcut = ShortcutInfoCompat.Builder(context, "shortcut_$filename")
            .setShortLabel("Web App")
            .setLongLabel("Aiyo Generated Web App")
            .setIcon(IconCompat.createWithResource(context, R.drawable.aiyo_icon))
            .setIntent(intent)
            .build()

        ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
    }
}
