# Keep Main Application Components and Activities
-keep class com.beradeep.aiyo.MainActivity { *; }
-keep class com.beradeep.aiyo.MainActivityDefault { *; }
-keep class com.beradeep.aiyo.AiyoApp { *; }
-keep class com.beradeep.aiyo.ui.screens.chat.WebAppActivity { *; }

# Keep WebAppActivity and its JavascriptInterface
-keep class com.beradeep.aiyo.ui.screens.chat.WebAppActivity { *; }
-keepclassmembers class com.beradeep.aiyo.ui.screens.chat.WebAppActivity$WebAppInterface {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep HtmlPreviewDialog's classes and JavascriptInterface
-keep class com.beradeep.aiyo.ui.screens.chat.components.HtmlPreviewDialogKt { *; }
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Preserve the line number information for debugging stack traces.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile