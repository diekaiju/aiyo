package com.beradeep.aiyo

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AiyoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if ((applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            android.webkit.WebView.setWebContentsDebuggingEnabled(true)
        }
    }
}
