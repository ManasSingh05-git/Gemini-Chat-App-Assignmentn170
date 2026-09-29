# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep line number information to hide the original source file name.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Room ---
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.RoomDatabase$Builder
-keep class * extends androidx.room.RoomDatabase$Callback
-dontwarn androidx.room.**
-keep @androidx.room.Entity class * { *; }

# --- Kotlinx Coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# --- DataStore ---
-keep class androidx.datastore.** { *; }

# --- Generative AI ---
-keep class com.google.ai.client.generativeai.** { *; }
