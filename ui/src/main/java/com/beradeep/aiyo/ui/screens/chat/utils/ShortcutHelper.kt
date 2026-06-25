package com.beradeep.aiyo.ui.screens.chat.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.beradeep.aiyo.ui.screens.chat.WebAppActivity
import java.io.File
import java.util.UUID

object ShortcutHelper {
    fun addWebAppToHomeScreen(
        context: Context,
        htmlContent: String,
        appName: String,
        emoji: String,
        backgroundColorHex: String
    ) {
        // Save HTML to a local file
        val filename = "webapp_${UUID.randomUUID()}.html"
        val file = File(context.filesDir, filename)
        file.writeText(htmlContent)

        // Create Intent
        val intent = Intent(context, WebAppActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra("FILE_PATH", file.absolutePath)
        }

        // Generate dynamic icon using the emoji
        val iconBitmap = createEmojiIcon(emoji, backgroundColorHex)
        val icon = IconCompat.createWithBitmap(iconBitmap)

        // Create Shortcut
        val shortcut = ShortcutInfoCompat.Builder(context, "shortcut_$filename")
            .setShortLabel(appName)
            .setLongLabel(appName)
            .setIcon(icon)
            .setIntent(intent)
            .build()

        ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
    }

    private fun createEmojiIcon(emoji: String, backgroundColorHex: String): Bitmap {
        val size = 512 // Icon size in pixels (large for crisp display)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Parse color
        val color = try {
            Color.parseColor(backgroundColorHex)
        } catch (e: Exception) {
            Color.parseColor("#5E35B1") // Default Electric Indigo fallback
        }

        // Draw background circle
        val paint = Paint().apply {
            isAntiAlias = true
            this.color = color
            style = Paint.Style.FILL
        }
        val radius = size / 2f
        canvas.drawCircle(radius, radius, radius, paint)

        // Draw emoji
        val textPaint = Paint().apply {
            isAntiAlias = true
            textSize = size * 0.55f // Scale text to fit inside the circle nicely
            textAlign = Paint.Align.CENTER
        }

        // Vertically center the text
        val bounds = Rect()
        textPaint.getTextBounds(emoji, 0, emoji.length, bounds)
        val yPos = radius + bounds.height() / 2f - bounds.bottom

        canvas.drawText(emoji, radius, yPos, textPaint)
        return bitmap
    }
}
