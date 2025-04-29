# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep data classes used by Gson
-keep class com.technovix.quiznova.data.remote.dto.** { *; }
-keepnames class com.technovix.quiznova.data.remote.dto.** { *; }

-keep class com.technovix.quiznova.data.local.entity.** { *; }
-keepnames class com.technovix.quiznova.data.local.entity.** { *; }

# Keep Room DAO Interfaces
-keep interface com.technovix.quiznova.data.local.QuizDao { *; }

# Keep members for GSON (needed if you use @SerializedName)
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}