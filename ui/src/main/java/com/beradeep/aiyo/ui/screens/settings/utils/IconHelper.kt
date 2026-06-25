package com.beradeep.aiyo.ui.screens.settings.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object IconHelper {
    enum class AppIcon(val aliasName: String, val displayName: String) {
        DEFAULT("com.beradeep.aiyo.MainActivityDefault", "Electric Indigo (Default)"),
        PINK("com.beradeep.aiyo.MainActivityPink", "Neon Pink"),
        DARK("com.beradeep.aiyo.MainActivityDark", "AMOLED Dark"),
        BLUE("com.beradeep.aiyo.MainActivityBlue", "Classic Blue")
    }

    fun setAppIcon(context: Context, targetIcon: AppIcon) {
        val packageManager = context.packageManager
        AppIcon.entries.forEach { icon ->
            val componentName = ComponentName(context, icon.aliasName)
            val state = if (icon == targetIcon) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }
            packageManager.setComponentEnabledSetting(
                componentName,
                state,
                PackageManager.DONT_KILL_APP
            )
        }
    }

    fun getCurrentIcon(context: Context): AppIcon {
        val packageManager = context.packageManager
        AppIcon.entries.forEach { icon ->
            val componentName = ComponentName(context, icon.aliasName)
            val enabledSetting = packageManager.getComponentEnabledSetting(componentName)
            if (enabledSetting == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                return icon
            }
        }
        return AppIcon.DEFAULT
    }
}
