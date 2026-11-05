# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Preserve source file and line numbers for stack traces
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature,InnerClasses,EnclosingMethod,*Annotation*

# ============================================================================
# 1. Room Entities, DAOs, and Database Keep Rules
# ============================================================================
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep class com.example.data.model.** { *; }
-keep class com.example.data.local.** { *; }

# ============================================================================
# 2. Moshi (@JsonClass / @Json) & Kotlin Reflection Keep Rules
# ============================================================================
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers class * {
    @com.squareup.moshi.FromJson <methods>;
    @com.squareup.moshi.ToJson <methods>;
}
-keep class **JsonAdapter { *; }

# ============================================================================
# 3. Retrofit, OkHttp & Remote / Repository DTO Keep Rules
# ============================================================================
-keep class com.example.data.remote.** { *; }
-keep class com.example.data.repository.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
